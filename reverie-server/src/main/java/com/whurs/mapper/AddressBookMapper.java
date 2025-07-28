package com.whurs.mapper;

import com.whurs.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    /**
     * 动态查询所有地址
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 插入地址数据
     * @param addressBook
     */
    @Insert("insert into address_book(user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) "
    +"values (#{userId},#{consignee},#{sex},#{phone},#{provinceCode},#{provinceName},#{cityCode},#{cityName},#{districtCode},#{districtName},#{detail},#{label},#{isDefault})")
    void insert(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Select("select * from address_book where id=#{id}")
    AddressBook getById(Long id);

    /**
     * 根据id修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    @Delete("delete from address_book where id=#{id}")
    void delete(Long id);

    /**
     * 修改当前用户所有地址为非默认
     * @param addressBook
     */
    /**
     * 根据 用户id修改 是否默认地址
     * @param addressBook
     */
    @Update("update address_book set is_default = #{isDefault} where user_id = #{userId}")
    void updateIsDefaultByUserId(AddressBook addressBook);
}
