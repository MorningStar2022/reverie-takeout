package com.whurs.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.whurs.constant.MessageConstant;
import com.whurs.constant.PasswordConstant;
import com.whurs.constant.StatusConstant;
import com.whurs.context.BaseContext;
import com.whurs.dto.EmployeeDTO;
import com.whurs.dto.EmployeeLoginDTO;
import com.whurs.dto.EmployeePageQueryDTO;
import com.whurs.entity.Employee;
import com.whurs.exception.AccountLockedException;
import com.whurs.exception.AccountNotFoundException;
import com.whurs.exception.PasswordErrorException;
import com.whurs.mapper.EmployeeMapper;
import com.whurs.result.PageResult;
import com.whurs.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密，然后再进行比对
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void saveEmp(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置默认状态，启用
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码，123456并加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //从threadlocal变量中取出当前用户的id，设置创建/更新该用户的用户id为当前用户的id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insertEmp(employee);
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }

    /**
     * 启用或禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee=new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @Override
    public void updateUser(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
//        employee.setUpdateUser(BaseContext.getCurrentId());
//        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }

}
