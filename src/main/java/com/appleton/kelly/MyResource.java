package com.appleton.kelly;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.appleton.kelly.business.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

// 1
/**
 * Root resource (exposed at "myresource" path)
 */
@Path("CompanyServices")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    // 3 - Delete all departments, employees, and timecards for a company
    @Path("/company")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCompany(@DefaultValue("kda9036") @QueryParam("company") String company) {

        BusinessLayer bl = new BusinessLayer();
        String dept = bl.deleteCompany(company);
        return Response.ok(dept).build();

    }// deleteCompany

    // 4 - Get a given department for a given company using dept_id
    @Path("/department")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDepartment(@DefaultValue("kda9036") @QueryParam("company") String companyName,
            @QueryParam("dept_id") int dept_id) {

        BusinessLayer bl = new BusinessLayer();
        String dept = bl.getDepartment(companyName, dept_id);
        return Response.ok(dept).build();

    }// getDepartment

    // 5 - Get all departments for a given company
    @Path("/departments")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDepartment(@DefaultValue("kda9036") @QueryParam("company") String companyName) {

        BusinessLayer bl = new BusinessLayer();
        String depts = bl.getDepartments(companyName);
        return Response.ok(depts).build();

    }// getAllDepartment

    // 6 - Update a department
    @Path("/department")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDepartment(String deptIn) {

        BusinessLayer bl = new BusinessLayer();
        String dept = bl.updateDepartment(deptIn);

        return Response.ok(dept).build();

    }// updateDepartment

    // 7 - Insert a department
    @Path("/department")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertDepartment(
            @FormParam("company") String companyName,
            @FormParam("dept_name") String deptName,
            @FormParam("dept_no") String deptNo,
            @FormParam("location") String loc) {

        BusinessLayer bl = new BusinessLayer();
        String dept = bl.insertDepartment(companyName, deptName, deptNo, loc);

        return Response.ok(dept).build();

    }// insertDepartment

    // 8 - Delete a given department for a given company
    @Path("/department")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDepartment(
            @DefaultValue("kda9036") @QueryParam("company") String companyName, @QueryParam("dept_id") int id) {

        BusinessLayer bl = new BusinessLayer();
        String dept = bl.deleteDepartment(companyName, id);

        return Response.ok(dept).build();

    }// deleteDepartment

    // 9 - Get requested employee
    @Path("/employee")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmployee(
            @DefaultValue("kda9036") @QueryParam("company") String companyName,
            @QueryParam("emp_id") int id) {

        BusinessLayer bl = new BusinessLayer();
        String cards = bl.getEmployee(companyName, id);

        return Response.ok(cards).build();

    }// getEmployee

    // 10
    @Path("/employees")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEmployee(@DefaultValue("kda9036") @QueryParam("company") String companyName) {

        BusinessLayer bl = new BusinessLayer();
        String depts = bl.getEmployees(companyName);
        return Response.ok(depts).build();

    }// getAllEmployee

    // 11 - Insert an employee
    @Path("/employee")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertEmployee(
            @DefaultValue("kda9036") @FormParam("company") String companyName,
            @FormParam("emp_name") String empName,
            @FormParam("emp_no") String empNo,
            @FormParam("hire_date") String hireDate,
            @FormParam("job") String job,
            @FormParam("salary") double salary,
            @FormParam("dept_id") int deptId,
            @FormParam("mng_id") int mngId) {

        BusinessLayer bl = new BusinessLayer();
        String emp = bl.insertEmployee(companyName, empName, empNo, hireDate, job, salary, deptId, mngId);

        return Response.ok(emp).build();

    }// insertEmployee

    // 12 - Update an employee
    @Path("/employee")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEmployee(String empIn) {

        BusinessLayer bl = new BusinessLayer();
        String emp = bl.updateEmployee(empIn);

        return Response.ok(emp).build();

    }// updateEmployee

    // 13 - Delete a given employee
    @Path("/employee")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEmployee(
            @DefaultValue("kda9036") @QueryParam("company") String companyName, @QueryParam("emp_id") int id) {

        BusinessLayer bl = new BusinessLayer();
        String emp = bl.deleteEmployee(companyName, id);

        return Response.ok(emp).build();

    }// deleteEmployee

    // 14 - Get requested timecard
    @Path("/timecard")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimecard(
            @DefaultValue("kda9036") @QueryParam("company") String companyName,
            @QueryParam("timecard_id") int id) {

        BusinessLayer bl = new BusinessLayer();
        String cards = bl.getTimecard(companyName, id);

        return Response.ok(cards).build();

    }// getTimecard

    // 15 - Get all timecards for an employee
    @Path("/timecards")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimecards(
            @DefaultValue("kda9036") @QueryParam("company") String companyName,
            @QueryParam("emp_id") int empId) {

        BusinessLayer bl = new BusinessLayer();
        String cards = bl.getAllTimecards(companyName, empId);

        return Response.ok(cards).build();

    }// getTimecards

    // 16 - Insert new timecard
    @Path("/timecard")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertTimecard(
            @FormParam("company") String companyName,
            @FormParam("emp_id") int empId,
            @FormParam("start_time") String start,
            @FormParam("end_time") String end) {

        BusinessLayer bl = new BusinessLayer();
        String card = bl.insertTimecard(companyName, empId, start, end);

        return Response.ok(card).build();

    }// insertTimecard

    // 17 - Update a timecard
    @Path("/timecard")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTimecard(String cardIn) {

        BusinessLayer bl = new BusinessLayer();
        String card = bl.updateTimecard(cardIn);

        return Response.ok(card).build();

    }// updateTimecard

    // 18 - Delete a given timecard
    @Path("/timecard")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTimecard(
            @DefaultValue("kda9036") @QueryParam("company") String companyName, @QueryParam("timecard_id") int id) {

        BusinessLayer bl = new BusinessLayer();
        String emp = bl.deleteTimecard(companyName, id);

        return Response.ok(emp).build();

    }// deleteTimecard

}// MyResource
