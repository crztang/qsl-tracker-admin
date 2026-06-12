package com.qsl.tracker.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RbacMapper {

    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.enabled = 1
            """)
    List<String> selectRoleCodes(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.code
            FROM sys_permission p
            JOIN sys_role_permission rp ON rp.permission_id = p.id
            JOIN sys_role r ON r.id = rp.role_id
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.enabled = 1 AND p.enabled = 1
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.enabled = 1 AND r.data_scope = 'ALL'
            """)
    long countAllDataScope(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO sys_user_role(user_id, role_id)
            SELECT #{userId}, id FROM sys_role WHERE code = #{roleCode}
            """)
    int assignRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
