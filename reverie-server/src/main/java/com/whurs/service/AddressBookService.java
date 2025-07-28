package com.whurs.service;

import com.whurs.entity.AddressBook;
import com.whurs.result.Result;

import java.util.List;

public interface AddressBookService {
    /**
     * 动态查询地址
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 新增地址
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 根据id修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteById(Long id);
}
