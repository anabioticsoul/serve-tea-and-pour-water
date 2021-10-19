package com.ruoyi.web.service;

import com.ruoyi.web.crypto.Entity.userCipher;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class sqlService {
    /**
     * 获取存储在数据库的用户密文信息
     *
     * @param userId 用户id
     * @return 用户密文信息
     */
    public userCipher getCipher(String userId) throws IOException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream is = Resources.getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session = sessionFactory.openSession();
        String statement = "com.ruoyi.web.crypto.mapping.userCipherMapper.getUser";
        userCipher usercipher = session.selectOne(statement, userId);
        System.out.println(usercipher);
        return usercipher;
    }

    /**
     * 将密文插入到数据库
     *
     * @param userId                      用户id
     * @param userPrivateKey              生成的私钥
     * @param blindedCertificateSignature 混淆后的签名
     */
    public void insertUserCipher(String userId, String userPrivateKey, String blindedCertificateSignature) throws IOException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream is = Resources.getResourceAsStream(resource);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session = sessionFactory.openSession();
        String statement = "com.ruoyi.web.crypto.mapping.userCipherMapper.save";
        userCipher userCipher = new userCipher(userId, userPrivateKey, blindedCertificateSignature);
        session.insert(statement, userCipher);
        session.commit();
    }
}
