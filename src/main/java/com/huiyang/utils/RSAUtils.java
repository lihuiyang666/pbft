package com.huiyang.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {
    /**
     * 随机生成密钥对
     * @throws NoSuchAlgorithmException
     */
    public static String[] genKeyPair() throws NoSuchAlgorithmException {

        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(512,new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        // 得到私钥字符串
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        String[] res=new String[2];
        res[0]=publicKeyString;
        res[1]=privateKeyString;
        // 将公钥和私钥保存到Map
        return  res;

    }
    //对String公钥加密
    public static String encrypt( String str, String publicKey ) throws Exception{
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }
    /*
    * 私钥解谜
    *
    */
    public static String decrypt(String str, String privateKey) throws Exception{
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }
    //对String生成私钥签名
    public static byte[] sign(String data,String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] data2=Base64.decodeBase64(data);
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(priKey);
        signature.update(data2);
        return signature.sign();



    }
    //公钥验签
    public static boolean verify(String data,byte[] sign,String publicKey) throws Exception {
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        Signature sig = Signature.getInstance("MD5withRSA");
        sig.initVerify(pubKey);
        byte[] data2=Base64.decodeBase64(data);
        sig.update(data2);
        return sig.verify(sign);


    }





    public static void main(String[] args) throws Exception {
        String[] res=genKeyPair();
        String message = "lihuiyang";
//        System.out.println("随机生成的公钥为:" + res[0]);
//        System.out.println("随机生成的私钥为:" + res[1]);
//        //用公钥进行加密
//        String messageEn = encrypt(message,res[0]);
//        System.out.println(message + "\t加密后的字符串为:" + messageEn);
//        //用私钥进行解密
//        String messageDe = decrypt(messageEn,res[1]);
//        System.out.println("还原后的字符串为:" + messageDe);
//        System.out.println(message.equals(messageDe));
        byte[] sign=sign(message,res[1]);
        System.out.println("产生签名："+Base64.encodeBase64String(sign));
        System.out.println(verify(message, sign, res[0]));


    }




}
