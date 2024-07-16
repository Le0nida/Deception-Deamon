package io.swagger.model;

import javax.persistence.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * VulnerableUser
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2024-05-15T17:26:22.532+02:00[Europe/Berlin]")


@Entity
@Table(name = "jnktkmz_vulnuser")
public class VulnerableUser   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id = null;

    @Column
    @JsonProperty("first_name")
    private String firstName = null;

    @Column
    @JsonProperty("last_name")
    private String lastName = null;

    @Column
    @JsonProperty("email")
    private String email = null;

    @Column
    @JsonProperty("username")
    private String username = null;

    @Column
    @JsonProperty("password")
    private String password = null;

    @Column
    @JsonProperty("gender")
    private String gender = null;

    @Column
    @JsonProperty("ipaddress")
    private String ipaddress = null;

    @Column
    @JsonProperty("address_id")
    private String addressId = null;

    @Column
    @JsonProperty("phone_number")
    private String phoneNumber = null;

    @Column
    @JsonProperty("job_title")
    private String jobTitle = null;

    @Column
    @JsonProperty("department")
    private String department = null;

    @Column
    @JsonProperty("company_id")
    private String companyId = null;

    @Column
    @JsonProperty("is_active")
    private String isActive = null;

    public VulnerableUser id(Long id) {
        this.id = id;
        return this;
    }

    /**
     * id autoincrementale
     * @return id
     **/
    @Schema(example = "10", description = "id autoincrementale")

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VulnerableUser firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Get firstName
     * @return firstName
     **/
    @Schema(example = "Barret", description = "")

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public VulnerableUser lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Get lastName
     * @return lastName
     **/
    @Schema(example = "Clere", description = "")

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public VulnerableUser email(String email) {
        this.email = email;
        return this;
    }

    /**
     * Get email
     * @return email
     **/
    @Schema(example = "bclered9@ask.com", description = "")

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public VulnerableUser username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     * @return username
     **/
    @Schema(example = "bclered9", description = "")

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public VulnerableUser password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get password
     * @return password
     **/
    @Schema(example = "$2a$04$7gKNwALhhhdAVcEBHYskI.EJkcGRFLdeMWCq51WU4CoxDwMQmlTU6", description = "")

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VulnerableUser gender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * Get gender
     * @return gender
     **/
    @Schema(example = "Male", description = "")

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public VulnerableUser ipaddress(String ipaddress) {
        this.ipaddress = ipaddress;
        return this;
    }

    /**
     * Get ipaddress
     * @return ipaddress
     **/
    @Schema(example = "143.182.83.110", description = "")

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public VulnerableUser addressId(String addressId) {
        this.addressId = addressId;
        return this;
    }

    /**
     * Get addressId
     * @return addressId
     **/
    @Schema(example = "478", description = "")

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public VulnerableUser phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Get phoneNumber
     * @return phoneNumber
     **/
    @Schema(example = "+86 (973) 677-1142", description = "")

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public VulnerableUser jobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    /**
     * Get jobTitle
     * @return jobTitle
     **/
    @Schema(example = "Associate Professor", description = "")

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public VulnerableUser department(String department) {
        this.department = department;
        return this;
    }

    /**
     * Get department
     * @return department
     **/
    @Schema(example = "Accounting", description = "")

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public VulnerableUser companyId(String companyId) {
        this.companyId = companyId;
        return this;
    }

    /**
     * Get companyId
     * @return companyId
     **/
    @Schema(example = "Edgetag", description = "")

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public VulnerableUser isActive(String isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Get isActive
     * @return isActive
     **/
    @Schema(example = "true", description = "")

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VulnerableUser user = (VulnerableUser) o;
        return Objects.equals(this.id, user.id) &&
                Objects.equals(this.firstName, user.firstName) &&
                Objects.equals(this.lastName, user.lastName) &&
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.username, user.username) &&
                Objects.equals(this.password, user.password) &&
                Objects.equals(this.gender, user.gender) &&
                Objects.equals(this.ipaddress, user.ipaddress) &&
                Objects.equals(this.addressId, user.addressId) &&
                Objects.equals(this.phoneNumber, user.phoneNumber) &&
                Objects.equals(this.jobTitle, user.jobTitle) &&
                Objects.equals(this.department, user.department) &&
                Objects.equals(this.companyId, user.companyId) &&
                Objects.equals(this.isActive, user.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, username, password, gender, ipaddress, addressId, phoneNumber, jobTitle, department, companyId, isActive);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class VulnerableUser {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
        sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    gender: ").append(toIndentedString(gender)).append("\n");
        sb.append("    ipaddress: ").append(toIndentedString(ipaddress)).append("\n");
        sb.append("    addressId: ").append(toIndentedString(addressId)).append("\n");
        sb.append("    phoneNumber: ").append(toIndentedString(phoneNumber)).append("\n");
        sb.append("    jobTitle: ").append(toIndentedString(jobTitle)).append("\n");
        sb.append("    department: ").append(toIndentedString(department)).append("\n");
        sb.append("    companyId: ").append(toIndentedString(companyId)).append("\n");
        sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
