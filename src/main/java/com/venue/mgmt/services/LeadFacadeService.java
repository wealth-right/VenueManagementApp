//package com.venue.mgmt.services;
//
//import static com.venue.mgmt.constant.GeneralMsgConstants.*;
//import com.venue.mgmt.entities.LeadRegistration;
//import com.venue.mgmt.request.CustomerRequest;
//import com.venue.mgmt.request.CustomerServiceClient;
//import com.venue.mgmt.request.UserMasterRequest;
//import com.venue.mgmt.services.impl.utils.OccupationCodesUtil;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//public class LeadFacadeService {
//
//    private static final Logger logger = LogManager.getLogger(LeadFacadeService.class);
//
//    private final UserMgmtResService userMgmtResService;
//
//    LeadFacadeService(UserMgmtResService userMgmtResService) {
//        this.userMgmtResService = userMgmtResService;
//    }
//
//
//
//    public String persistCustomerDetails(String userId, LeadRegistration leadRegistration, String authHeader) {
//        logger.info("VenueManagementApp - Inside persistCustomerDetails Method");
//        UserMasterRequest userMasterDetails = userMgmtResService.getUserMasterDetails(userId);
//        if(userMasterDetails == null){
//            return null;
//        }
//        CustomerRequest customerRequest = new CustomerRequest();
//        if ((!leadRegistration.getFullName().isEmpty()) && leadRegistration.getFullName() != null) {
//            customerRequest.setFirstname(leadRegistration.getFullName().split(" ")[0]);
//            customerRequest.setMiddlename(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
//            customerRequest.setLastname(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
//        }
//        customerRequest.setFullname(leadRegistration.getFullName());
//        customerRequest.setEmailid(leadRegistration.getEmail());
//        customerRequest.setCountrycode(COUNTRY_CODE);
//        customerRequest.setMobileno(leadRegistration.getMobileNumber());
//        customerRequest.setAddedUpdatedBy(userId);
//        if (leadRegistration.getGender() != null && (!leadRegistration.getGender().isEmpty())) {
//            customerRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
//            if (leadRegistration.getGender().equalsIgnoreCase(MALE)) {
//                customerRequest.setTitle("Mr.");
//            } else if (leadRegistration.getGender().equalsIgnoreCase(FEMALE) && leadRegistration.getMaritalStatus() != null
//                    && (!leadRegistration.getMaritalStatus().isEmpty())
//                    && leadRegistration.getMaritalStatus().equalsIgnoreCase("Married")) {
//                customerRequest.setTitle("Mrs.");
//            } else {
//                customerRequest.setTitle("Miss.");
//            }
//        }
//        String occupation=null;
//        if(leadRegistration.getOccupation()!=null && (!leadRegistration.getOccupation().isEmpty())){
//            occupation = OccupationCodesUtil.mapOccupationToCode(leadRegistration.getOccupation());
//        }
//        customerRequest.setOccupation(occupation);
//        customerRequest.setTaxStatus("01");
//        customerRequest.setCountryOfResidence("India");
//        customerRequest.setSource("QuickTapApp");
//        customerRequest.setCustomertype("Prospect");
//        customerRequest.setChannelcode(userMasterDetails.getChannelCode());
//        customerRequest.setBranchCode(userMasterDetails.getBranchCode());
//        CustomerServiceClient customerServiceClient = new CustomerServiceClient();
//        ResponseEntity<String> entity = customerServiceClient.saveCustomerData(customerRequest,authHeader);
//        return entity.getBody();
//    }
//}
