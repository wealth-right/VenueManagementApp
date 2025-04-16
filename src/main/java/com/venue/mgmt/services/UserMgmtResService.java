package com.venue.mgmt.services;

import com.venue.mgmt.request.CustomerRequest;
import com.venue.mgmt.request.UserMasterRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
@Service
public class UserMgmtResService {

    private final JdbcTemplate jdbcTemplate;

    public UserMgmtResService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public  List<Map<String, Object>> getDataFromOtherSchema(String branchcode) {
        String sql = "select branch_name,branchcode " +
                "from usermgmt.usermaster u,usermgmt.branch_master b\n" +
                "where u.branchcode=b.branch_code and u.branchcode = ? limit 1";
        return jdbcTemplate.queryForList(sql,branchcode);
    }

    public CustomerRequest getCustomerDetails(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return null;
        }
        try {
            String sql = "select * from customerservice.customer where customerid = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, customerRowMapper());
        }
        catch (Exception e) {
            return new CustomerRequest();
        }
    }

    public UserMasterRequest getUserMasterDetails(String userId){
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        String sql = "select id,user_id,first_name,last_name,mobile_number,email_id,branchcode,channelcode " +
                "from usermgmt.usermaster where user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{userId}, userMasterRowMapper());
        } catch (Exception e) {
            return new UserMasterRequest();
        }
    }

    private RowMapper<UserMasterRequest> userMasterRowMapper(){
        return (rs, rowNum) -> {
            UserMasterRequest userMasterRequest = new UserMasterRequest();
            userMasterRequest.setId(rs.getString("id"));
            userMasterRequest.setUserId(rs.getString("user_id"));
            userMasterRequest.setFirstName(rs.getString("first_name"));
            userMasterRequest.setLastName(rs.getString("last_name"));
            userMasterRequest.setMobileNumber(rs.getString("mobile_number"));
            userMasterRequest.setEmailId(rs.getString("email_id"));
            userMasterRequest.setBranchCode(rs.getString("branchcode"));
            userMasterRequest.setChannelCode(rs.getString("channelcode"));
            return userMasterRequest;
        };
    }

    private RowMapper<CustomerRequest> customerRowMapper() {
        return (rs, rowNum) -> {
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setFirstname(rs.getString("firstname"));
            customerRequest.setMiddlename(rs.getString("middlename"));
            customerRequest.setLastname(rs.getString("lastname"));
            customerRequest.setFullname(rs.getString("fullname"));
            customerRequest.setEmailid(rs.getString("emailid"));
            customerRequest.setCountrycode(rs.getString("countrycode"));
            customerRequest.setMobileno(rs.getString("mobileno"));
            customerRequest.setAddedUpdatedBy(rs.getString("addedby"));
            customerRequest.setAssignedto(rs.getString("assignedto"));
            customerRequest.setGender(rs.getString("gender"));
            customerRequest.setTitle(rs.getString("title"));
            customerRequest.setOccupation(rs.getString("occupation"));
            customerRequest.setTaxStatus(rs.getString("taxStatus"));
            customerRequest.setCountryOfResidence(rs.getString("countryOfResidence"));
            customerRequest.setSource(rs.getString("source"));
            customerRequest.setCustomerId(rs.getString("customerid"));
            customerRequest.setCustomertype(rs.getString("customertype"));
            customerRequest.setChannelcode(rs.getString("channelcode"));
            customerRequest.setBranchCode(rs.getString("branchcode"));
            return customerRequest;
        };
    }

}
