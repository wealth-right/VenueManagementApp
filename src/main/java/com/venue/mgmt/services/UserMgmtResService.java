package com.venue.mgmt.services;

import org.springframework.jdbc.core.JdbcTemplate;
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
                "where u.branchcode=b.branch_code and u.branchcode = ?";
        return jdbcTemplate.queryForList(sql,branchcode);
    }

}
