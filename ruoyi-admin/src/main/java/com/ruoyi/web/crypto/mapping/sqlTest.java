package com.ruoyi.web.crypto.mapping;

import com.ruoyi.web.crypto.Entity.userCipher;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class sqlTest {
    @Test
    public void test() throws IOException {
        String resource = "mybatis/mybatis-config.xml";
        //使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）

        InputStream is = Resources.getResourceAsStream(resource);
        //构建sqlSession的工厂

        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        //使用MyBatis提供的Resources类加载mybatis的配置文件（它也加载关联的映射文件）
        //Reader reader = Resources.getResourceAsReader(resource);
        //构建sqlSession的工厂
        //SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader);
        //创建能执行映射文件中sql的sqlSession
        SqlSession session = sessionFactory.openSession();

        /* 根据——key查找 */
//        String statement = "com.ruoyi.web.crypto.mapping.userCipherMapper.getUser";
//        //映射sql的标识字符串，getUser与映射文件中配置select标签id一致
//        //执行查询返回一个唯一user对象的sql
//        userCipher user = session.selectOne(statement, "0485");
//        System.out.println(user);
        String statement2 = "com.ruoyi.web.crypto.mapping.userCipherMapper.save";
        userCipher user2 = new userCipher("2012", "213312", "12321312");
        int result = session.insert(statement2, user2);
        session.commit();
        System.out.println(result);
    }
}
