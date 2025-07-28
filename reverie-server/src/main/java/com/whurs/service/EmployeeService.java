package com.whurs.service;

import com.whurs.dto.EmployeeDTO;
import com.whurs.dto.EmployeeLoginDTO;
import com.whurs.dto.EmployeePageQueryDTO;
import com.whurs.entity.Employee;
import com.whurs.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     *新增员工
     * @param employeeDTO
     */
    void saveEmp(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void updateUser(EmployeeDTO employeeDTO);
}
