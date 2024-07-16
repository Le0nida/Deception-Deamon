package io.swagger.api;

import io.swagger.log.LogService;
import io.swagger.model.User;
import io.swagger.model.VulnerableUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.*;

@Controller
@RequestMapping("/management")
public class VulnerableController {

    private final UserRepository userRepository;

    private final LogService logService;

    private final HttpServletRequest request;

    @Autowired
    public VulnerableController(UserRepository userRepository, LogService logService, HttpServletRequest request) {
        this.userRepository = userRepository;
        this.logService = logService;
        this.request = request;
    }

    // Simulated SQL Injection Vulnerability
    @GetMapping("/users-info")
    public String searchUsersPage(Model model) {
        logService.log(request);

        // Populate model with initial data
        model.addAttribute("searchTypes", Arrays.asList("username", "email", "first_name", "last_name", "gender", "job_title"));
        model.addAttribute("users", new ArrayList<>()); // Initial empty list of users
        return "vulnerabilities/users-info";
    }

    @PostMapping("/users-info")
    public String searchUsers(@RequestParam String searchType, @RequestParam String searchTerm, Model model) {
        logService.log(request);

        // Construct the SQL query with potential SQL injection vulnerability
        String query = "SELECT * FROM jnktkmz_vulnuser WHERE " + searchType + " = '" + searchTerm + "'";

        // Execute the potentially vulnerable query
        List<VulnerableUser> users = executeQuery(query);

        // Add the retrieved users to the model
        model.addAttribute("searchTypes", Arrays.asList("username", "email", "first_name", "last_name", "gender", "job_title"));
        model.addAttribute("users", users);

        // Return the template name for rendering
        return "vulnerabilities/users-info";
    }

    private List<VulnerableUser> executeQuery(String query) {
        List<VulnerableUser> users = new ArrayList<>();
        try {
            // Connessione al database (in un ambiente di produzione, utilizzare un pool di connessioni)
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/fakeDataDb?allowPublicKeyRetrieval=true", "root", "cybersecdcg");

            // Esecuzione della query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Elaborazione dei risultati
            while (resultSet.next()) {
                VulnerableUser user = new VulnerableUser();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setGender(resultSet.getString("gender"));
                user.setJobTitle(resultSet.getString("job_title"));
                users.add(user);
            }

            // Chiusura delle risorse
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Simulated XSS Vulnerability
    @GetMapping("/userprofile")
    public String xss(@RequestParam String username, Model model) {
        logService.log(request);

        boolean userExists = userRepository.existsByUsername(username);

        User user;
        if (userExists) {
            user = userRepository.findByUsername(username).get(0); // Presumendo che `findByUsername` ritorni una lista
        } else {
            user = new User();
            user.setUsername(username);
        }

        model.addAttribute("userExists", userExists);
        model.addAttribute("user", user);

        return "vulnerabilities/old-profile";
    }


    // Define commands and their simulated outputs
    private static final Map<String, String> safeCommands = new HashMap<>();
    static {
        safeCommands.put("ls", "README.md\nconfig.yaml\ndata.csv\nsrc\nbin\nlogs");
        safeCommands.put("ls -l", "total 12\n-rw-r--r-- 1 user user 1234 May 22 12:34 README.md\n-rw-r--r-- 1 user user 5678 May 22 12:34 config.yaml\n-rw-r--r-- 1 user user 9012 May 22 12:34 data.csv\ndrwxr-xr-x 2 user user 4096 May 22 12:34 src\ndrwxr-xr-x 2 user user 4096 May 22 12:34 bin\ndrwxr-xr-x 2 user user 4096 May 22 12:34 logs\n");
        safeCommands.put("ls -a", ".\n..\nREADME.md\nconfig.yaml\ndata.csv\nsrc\nbin\nlogs\n.hidden_file\n.hidden_folder");
        safeCommands.put("pwd", "/home/user/project");
        safeCommands.put("whoami", "admin");
        safeCommands.put("id", "uid=1000(admin) gid=1000(admin) groups=1000(admin),27(sudo)");
        safeCommands.put("date", "Wed May 22 12:34:56 UTC 2024");
        safeCommands.put("uptime", "12:34:56 up 10 days,  3:42,  2 users,  load average: 0.00, 0.01, 0.05");
        safeCommands.put("df -h", "Filesystem      Size  Used Avail Use% Mounted on\n/dev/sda1        50G   25G   25G  50% /\n/dev/sdb1       100G   60G   40G  60% /home");
        safeCommands.put("free -m", "              total        used        free      shared  buff/cache   available\nMem:           2000         600         500         100         900        1200\nSwap:          1000         200         800");
        safeCommands.put("ps aux", "USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND\nroot         1  0.0  0.1  18560  1056 ?        Ss   12:34   0:01 /sbin/init\nuser      1234  0.1  0.5  72364  5432 ?        Ss   12:35   0:05 /usr/bin/python3 /home/user/app.py\n");
        safeCommands.put("netstat -tuln", "Active Internet connections (only servers)\nProto Recv-Q Send-Q Local Address           Foreign Address         State\ntcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN\nudp        0      0 0.0.0.0:123             0.0.0.0:*\n");
        safeCommands.put("ifconfig", "eth0      Link encap:Ethernet  HWaddr 00:0c:29:4f:8e:35  \n          inet addr:192.168.0.100  Bcast:192.168.0.255  Mask:255.255.255.0\n          inet6 addr: fe80::20c:29ff:fe4f:8e35/64 Scope:Link\n          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1\n          RX packets:308 errors:0 dropped:0 overruns:0 frame:0\n          TX packets:268 errors:0 dropped:0 overruns:0 carrier:0\n          collisions:0 txqueuelen:1000 \n          RX bytes:34292 (34.2 KB)  TX bytes:29268 (29.2 KB)\nlo        Link encap:Local Loopback  \n          inet addr:127.0.0.1  Mask:255.0.0.0\n          inet6 addr: ::1/128 Scope:Host\n          UP LOOPBACK RUNNING  MTU:65536  Metric:1\n          RX packets:0 errors:0 dropped:0 overruns:0 frame:0\n          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0\n          collisions:0 txqueuelen:1 \n          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)");
        safeCommands.put("ping -c 1 google.com", "PING google.com (172.217.164.110) 56(84) bytes of data.\n64 bytes from 172.217.164.110: icmp_seq=1 ttl=54 time=15.6 ms\n\n--- google.com ping statistics ---\n1 packets transmitted, 1 received, 0% packet loss, time 0ms\nrtt min/avg/max/mdev = 15.602/15.602/15.602/0.000 ms");
        safeCommands.put("curl -I http://example.com", "HTTP/1.1 200 OK\nDate: Wed, 22 May 2024 12:34:56 GMT\nServer: Apache/2.4.29 (Ubuntu)\nContent-Type: text/html; charset=UTF-8\n");
        safeCommands.put("top -b -n1 | head -n 10", "top - 12:34:56 up 10 days,  3:42,  2 users,  load average: 0.00, 0.01, 0.05\nTasks: 123 total,   1 running, 122 sleeping,   0 stopped,   0 zombie\n%Cpu(s):  0.3 us,  0.1 sy,  0.0 ni, 99.5 id,  0.0 wa,  0.0 hi,  0.1 si,  0.0 st\nKiB Mem :  2000000 total,   600000 used,   500000 free,   100000 shared,   900000 buff/cache\nKiB Swap:  1000000 total,   200000 used,   800000 free.  1200000 avail Mem\n\n  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND\n    1 root      20   0   18560   1056    800 S   0.0  0.1   0:01.00 init\n 1234 user      20   0   72364   5432   2100 S   0.1  0.5   0:05.00 python3\n");
    }

    private static final Map<String, String> dangerousCommands = new HashMap<>();
    static {
        dangerousCommands.put("sudo", "Error: Permission denied.");
        dangerousCommands.put("rm -rf /", "Error: Permission denied.");
        dangerousCommands.put("shutdown -h now", "Error: Permission denied.");
        dangerousCommands.put("reboot", "Error: Permission denied.");
        dangerousCommands.put("dd if=/dev/zero of=/dev/sda", "Error: Permission denied.");
        dangerousCommands.put("mkfs.ext4 /dev/sda1", "Error: Permission denied.");
        dangerousCommands.put("iptables -F", "Error: Permission denied.");
        dangerousCommands.put("chmod 777 /etc/passwd", "Error: Permission denied.");
        dangerousCommands.put("kill -9 1", "Error: Permission denied.");
        dangerousCommands.put("usermod -L root", "Error: Permission denied.");
        dangerousCommands.put("cp /dev/null /etc/shadow", "Error: Permission denied.");
        dangerousCommands.put("dd if=/dev/zero of=/dev/mem", "Error: Permission denied.");
        dangerousCommands.put("rm -rf /home/user", "Error: Permission denied.");
        dangerousCommands.put("iptables -A INPUT -j DROP", "Error: Permission denied.");
        dangerousCommands.put("rm -rf /var/log", "Error: Permission denied.");
    }

    // Simulated Remote Code Execution (RCE) Vulnerability
    @GetMapping("/command-exec")
    public ResponseEntity<String> executeCommand(@RequestParam String command, @RequestParam(required = false) String pin) {
        logService.log(request);

        // Check if the command is safe or dangerous
        boolean isSafeCommand = safeCommands.containsKey(command);
        boolean isDangerousCommand = dangerousCommands.containsKey(command);

        // If it's a dangerous command and a pin is required
        if (isDangerousCommand && pin == null) {
            return ResponseEntity.ok("Error: A PIN is required to execute dangerous commands.");
        }

        // If it's a dangerous command and the pin is incorrect
        if (isDangerousCommand) {
            return ResponseEntity.ok("Error: Incorrect PIN. Command execution not authorized.");
        }

        // If it's a dangerous command and the pin is correct, or if it's a safe command
        String simulatedOutput;
        if (isSafeCommand) {
            simulatedOutput = safeCommands.get(command);
        } else {
            simulatedOutput = "Error: Command not recognized.";
        }

        // Return the simulated output as plain text
        return ResponseEntity.ok(simulatedOutput);
    }



    private static final Map<String, String> fakeFileContents = new HashMap<>();
    static {
        fakeFileContents.put("../../../etc/passwd",
                "root:x:0:0:root:/root:/bin/bash\n" +
                        "daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin\n" +
                        "bin:x:2:2:bin:/bin:/usr/sbin/nologin\n" +
                        "sys:x:3:3:sys:/dev:/usr/sbin/nologin\n" +
                        "sync:x:4:65534:sync:/bin:/bin/sync\n" +
                        "games:x:5:60:games:/usr/games:/usr/sbin/nologin\n" +
                        "man:x:6:12:man:/var/cache/man:/usr/sbin/nologin\n" +
                        "lp:x:7:7:lp:/var/spool/lpd:/usr/sbin/nologin\n" +
                        "mail:x:8:8:mail:/var/mail:/usr/sbin/nologin\n" +
                        "news:x:9:9:news:/var/spool/news:/usr/sbin/nologin\n" +
                        "uucp:x:10:10:uucp:/var/spool/uucp:/usr/sbin/nologin\n" +
                        "proxy:x:13:13:proxy:/bin:/usr/sbin/nologin\n" +
                        "www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin\n" +
                        "backup:x:34:34:backup:/var/backups:/usr/sbin/nologin\n" +
                        "list:x:38:38:Mailing List Manager:/var/list:/usr/sbin/nologin\n" +
                        "irc:x:39:39:ircd:/var/run/ircd:/usr/sbin/nologin\n" +
                        "gnats:x:41:41:Gnats Bug-Reporting System (admin):/var/lib/gnats:/usr/sbin/nologin\n" +
                        "nobody:x:65534:65534:nobody:/nonexistent:/usr/sbin/nologin\n" +
                        "systemd-timesync:x:100:103:systemd Time Synchronization,,,:/run/systemd:/usr/sbin/nologin\n" +
                        "systemd-network:x:101:104:systemd Network Management,,,:/run/systemd:/usr/sbin/nologin\n" +
                        "systemd-resolve:x:102:105:systemd Resolver,,,:/run/systemd:/usr/sbin/nologin\n" +
                        "systemd-bus-proxy:x:103:106:systemd Bus Proxy,,,:/run/systemd:/usr/sbin/nologin\n" +
                        "user:x:1000:1000:user:/home/user:/bin/bash\n");

        fakeFileContents.put("../../../etc/shadow",
                "root:$6$randomsalt$encryptedpassword:18000:0:99999:7:::\n" +
                        "daemon:*:17336:0:99999:7:::\n" +
                        "bin:*:17336:0:99999:7:::\n" +
                        "sys:*:17336:0:99999:7:::\n" +
                        "sync:*:17336:0:99999:7:::\n" +
                        "games:*:17336:0:99999:7:::\n" +
                        "man:*:17336:0:99999:7:::\n" +
                        "lp:*:17336:0:99999:7:::\n" +
                        "mail:*:17336:0:99999:7:::\n" +
                        "news:*:17336:0:99999:7:::\n" +
                        "uucp:*:17336:0:99999:7:::\n" +
                        "proxy:*:17336:0:99999:7:::\n" +
                        "www-data:*:17336:0:99999:7:::\n" +
                        "backup:*:17336:0:99999:7:::\n" +
                        "list:*:17336:0:99999:7:::\n" +
                        "irc:*:17336:0:99999:7:::\n" +
                        "gnats:*:17336:0:99999:7:::\n" +
                        "nobody:*:17336:0:99999:7:::\n" +
                        "systemd-timesync:*:17336:0:99999:7:::\n" +
                        "systemd-network:*:17336:0:99999:7:::\n" +
                        "systemd-resolve:*:17336:0:99999:7:::\n" +
                        "systemd-bus-proxy:*:17336:0:99999:7:::\n" +
                        "user:$6$randomsalt$encryptedpassword:18000:0:99999:7:::\n");

        fakeFileContents.put("../../../var/www/html/index.php",
                "<?php\n" +
                        "// Sample PHP file\n" +
                        "echo 'Hello, world!';\n" +
                        "?>");

        fakeFileContents.put("../../../var/log/auth.log",
                "Jul  4 15:05:01 localhost CRON[1234]: (root) CMD (cd / && run-parts --report /etc/cron.hourly)\n" +
                        "Jul  4 15:10:32 localhost sshd[9876]: Accepted password for user from 192.168.0.100 port 22 ssh2\n" +
                        "Jul  4 15:12:45 localhost sshd[9876]: pam_unix(sshd:session): session opened for user user by (uid=0)\n" +
                        "Jul  4 15:15:01 localhost sudo:     user : TTY=pts/1 ; PWD=/home/user ; USER=root ; COMMAND=/bin/ls\n" +
                        "Jul  4 15:15:01 localhost sudo: pam_unix(sudo:session): session opened for user root by user(uid=0)\n" +
                        "Jul  4 15:15:02 localhost sudo: pam_unix(sudo:session): session closed for user root\n");

        fakeFileContents.put("../home/user/.ssh/id_rsa",
                "-----BEGIN RSA PRIVATE KEY-----\n" +
                        "MIICXAIBAAKBgQC4QAUjC0B/b46hpXL+tWpHAV2Isj8MsuZva2w//8H4EAdyM0VP" +
                        "vBUsdJtmxBiImfzIK9DT0XYF0x5KyDDLr+ydJ58GCITkSiu8wm7fUo3UOGU+AHmb" +
                        "MSLFGjuFQ8JQ4EEMNa5m4KE3THAPmQoFQLee4+L7ajaixdzYopL+TrVvVQIDAQAB" +
                        "AoGAYkFZu+9zLMJL+K3nXidWjpkOzSfo8ZexxTcvGg4E3YmGvwLScovlarigarQm" +
                        "NSqRIBolmd+4pbCJFe4d/3FIpzgjk5oge9zJ3v6as/co+bII76efUBTR39r+2VMc" +
                        "uYMq+RWp96fspJGFn3pjiAZK2HhmYcZh//C424Ap2RQ5NZ0CQQD5wmHZIo4ElusE" +
                        "8Yf41mcuE7MVhAKqmt1pb3I0VyK63bC1RgEVCvUoH380zBd1ISSnEcs60k8sBJOl" +
                        "lbQ+Y3jPAkEAvNqZfinW9GaX01l5WppvT5umyuIzlEt4HEdJBgNYKa1O8SN3qWCQ" +
                        "FAXIlMyK9fxRcW0TpDglug3bdRgeba6WmwJBAKOY398K81uuP2ONkjM8vXlBDh0Z" +
                        "y2VU65MUnQoLyYP9krIqmKrf8pXFi91ZkkP24btNvIFbIOEEXnIoDViQb6ECQEEz" +
                        "Lz7xbvbd3JjpEQ6IttlDIah1w9z02TjaG4np6awp21FBL3TYk6V2AbPtkiQuwmdo" +
                        "gxl1EQFU+2lHtGmllvUCQE19J+luuDxc/lr34RN+qktx6+jRBt5g0cce9TjosUkE" +
                        "rYwe8oS62OuKqU8lRCLvQGEmrBrn5YedWfVL2QWVJeY=\n" +
                        "-----END RSA PRIVATE KEY-----");

        fakeFileContents.put("../home/user/.ssh/id_rsa.pub",
                "-----BEGIN PUBLIC KEY-----\n" +
                        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4QAUjC0B/b46hpXL+tWpHAV2I" +
                        "sj8MsuZva2w//8H4EAdyM0VPvBUsdJtmxBiImfzIK9DT0XYF0x5KyDDLr+ydJ58G" +
                        "CITkSiu8wm7fUo3UOGU+AHmbMSLFGjuFQ8JQ4EEMNa5m4KE3THAPmQoFQLee4+L7" +
                        "ajaixdzYopL+TrVvVQIDAQAB\n" +
                        "-----END PUBLIC KEY-----");

        fakeFileContents.put("../home/user/Documents/secret.txt",
                "VGhpcyBpcyBhIHNhbXBsZSBzZWNyZXQgZG9jdW1lbnQuIFBsZWFzZSBjb250YWN0\n" +
                        "IHlvdXIgc3lzdGVtIGFkbWluaXN0cmF0b3IgZm9yIG1vcmUgaW5mb3JtYXRpb24u");

        fakeFileContents.put("../../../etc/hosts",
                "127.0.0.1       localhost\n" +
                        "127.0.1.1       mymachine.localdomain mymachine\n" +
                        "# The following lines are desirable for IPv6 capable hosts\n" +
                        "::1     localhost ip6-localhost ip6-loopback\n" +
                        "ff02::1 ip6-allnodes\n" +
                        "ff02::2 ip6-allrouters\n");

        fakeFileContents.put("../../../etc/hostname", "machine438\n");

        fakeFileContents.put("../../../etc/resolv.conf",
                "nameserver 8.8.8.8\n" +
                        "nameserver 8.8.4.4\n");

        fakeFileContents.put("../../../var/log/dmesg",
                "[    0.000000] Initializing cgroup subsys cpuset\n" +
                        "[    0.000000] Initializing cgroup subsys cpu\n" +
                        "[    0.000000] Initializing cgroup subsys cpuacct\n" +
                        "[    0.000000] Linux version 4.15.0-20-generic (buildd@lgw01-amd64-052) (gcc version 7.3.0 (Ubuntu 7.3.0-16ubuntu3)) #21-Ubuntu SMP Tue Apr 24 06:15:27 UTC 2018 (Ubuntu 4.15.0-20.21-generic 4.15.17)\n" +
                        "[    0.000000] Command line: BOOT_IMAGE=/boot/vmlinuz-4.15.0-20-generic root=UUID=deadbeef-dead-beef-dead-beefdeadbeef ro quiet splash vt.handoff=1\n" +
                        "[    0.000000] KERNEL supported cpus:\n" +
                        "[    0.000000]   Intel GenuineIntel\n" +
                        "[    0.000000]   AMD AuthenticAMD\n" +
                        "[    0.000000]   Centaur CentaurHauls\n" +
                        "[    0.000000] x86/fpu: Supporting XSAVE feature 0x001: 'x87 floating point registers'\n" +
                        "[    0.000000] x86/fpu: Supporting XSAVE feature 0x002: 'SSE registers'\n" +
                        "[    0.000000] x86/fpu: Supporting XSAVE feature 0x004: 'AVX registers'\n");

        fakeFileContents.put("../../../var/log/apache2/access.log",
                "127.0.0.1 - - [04/Jul/2024:15:00:00 +0000] \"GET /index.php HTTP/1.1\" 200 45\n" +
                        "192.168.0.100 - - [04/Jul/2024:15:01:00 +0000] \"POST /login.php HTTP/1.1\" 302 -\n" +
                        "192.168.0.101 - - [04/Jul/2024:15:02:00 +0000] \"GET /dashboard.php HTTP/1.1\" 200 1024\n");

        fakeFileContents.put("../../../var/log/apache2/error.log",
                "[Wed Jul 04 15:00:00.000000 2024] [core:notice] [pid 1234] AH00094: Command line: '/usr/sbin/apache2'\n" +
                        "[Wed Jul 04 15:01:00.000000 2024] [php7:error] [pid 1235] [client 192.168.0.100:12345] PHP Fatal error:  Uncaught Error: Call to undefined function non_existent_function() in /var/www/html/index.php:10\n" +
                        "[Wed Jul 04 15:02:00.000000 2024] [mpm_prefork:notice] [pid 1234] AH00169: caught SIGTERM, shutting down\n");

        fakeFileContents.put("../home/user/.bash_history",
                "ls -la\n" +
                        "cd /var/www/html\n" +
                        "nano index.php\n" +
                        "ssh user@remotehost\n" +
                        "sudo apt-get update\n" +
                        "sudo apt-get upgrade\n");

        fakeFileContents.put("../home/user/.profile",
                "# ~/.profile: executed by the command interpreter for login shells.\n" +
                        "# This file is not read by bash(1), if ~/.bash_profile or ~/.bash_login\n" +
                        "# exists.\n" +
                        "# see /usr/share/doc/bash/examples/startup-files for examples.\n" +
                        "# the files are located in the bash-doc package.\n" +
                        "# the default umask is set in /etc/profile\n" +
                        "#umask 022\n" +
                        "# if running bash\n" +
                        "if [ -n \"$BASH_VERSION\" ]; then\n" +
                        "    # include .bashrc if it exists\n" +
                        "    if [ -f \"$HOME/.bashrc\" ]; then\n" +
                        "        . \"$HOME/.bashrc\"\n" +
                        "    fi\n" +
                        "fi\n" +
                        "# set PATH so it includes user's private bin if it exists\n" +
                        "if [ -d \"$HOME/bin\" ] ; then\n" +
                        "    PATH=\"$HOME/bin:$PATH\"\n" +
                        "fi\n");

        fakeFileContents.put("../../../etc/cron.d/sample-cron",
                "# /etc/cron.d/sample-cron: crontab entries for sample application\n" +
                        "# m h dom mon dow user  command\n" +
                        "0 1 * * * root /usr/local/bin/sample-cron-job\n");
    }

    // Simulated Directory Traversal Vulnerability
    @GetMapping("/file")
    public ResponseEntity<String> directoryTraversal(@RequestParam String filePath) {
        logService.log(request);

        // Retrieve the fake file content
        String fakeFileContent = fakeFileContents.getOrDefault(filePath, "File not found: " + filePath);

        // Return the content as plain text
        return ResponseEntity.ok(fakeFileContent);
    }



    @GetMapping("/user-management")
    public ResponseEntity<String> userManagement() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    @GetMapping("/user-profile")
    public ResponseEntity<String> userProfile() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    @GetMapping("/server-commands")
    public ResponseEntity<String> serverCommands() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    @GetMapping("/file-access")
    public ResponseEntity<String> fileAccess() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    @GetMapping("/admin-dashboard")
    public ResponseEntity<String> adminDashboard() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    @GetMapping("/user-logs")
    public ResponseEntity<String> userLogs() {
        logService.log(request);
        return deprecatedResourcePage();
    }

    private ResponseEntity<String> deprecatedResourcePage() {
        logService.log(request);
        String htmlContent = "<html><body><h1>Resource Not Managed</h1><p>This resource is no longer managed.</p></body></html>";
        return ResponseEntity.ok(htmlContent);
    }
}

