package com.venue.mgmt.services;

import com.venue.mgmt.request.CustomerRequest;
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
        String sql = "select * from customerservice.customer where customerid = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, customerRowMapper());
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
            customerRequest.setCustomertype(rs.getString("customertype"));
            customerRequest.setChannelcode(rs.getString("channelcode"));
            customerRequest.setBranchCode(rs.getString("branchcode"));
            return customerRequest;
        };
    }

}
