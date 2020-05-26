package com.tencent.wework;

/* sdk返回数据
typedef struct Slice_t {
    char* buf;
    int len;
} Slice_t;

typedef struct MediaData {
    char* outindexbuf;
    int out_len;
    char* data;
    int data_len;
    int is_finish;
} MediaData_t;
*/

import com.tencent.enums.DllSuffixEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class Finance {
    public native static long NewSdk();

    /**
     * 初始化函数
     * Return值=0表示该API调用成功
     *
     * @param sdk    NewSdk返回的sdk指针
     * @param corpid 调用企业的企业id，例如：wwd08c8exxxx5ab44d，可以在企业微信管理端--我的企业--企业信息查看
     * @param secret 聊天内容存档的Secret，可以在企业微信管理端--管理工具--聊天内容存档查看
     * @return 返回是否初始化成功
     * 0   - 成功
     * !=0 - 失败
     */
    public native static int Init(long sdk, String corpid, String secret);

    /**
     * 拉取聊天记录函数
     * Return值=0表示该API调用成功
     *
     * @param sdk      NewSdk返回的sdk指针
     * @param seq      从指定的seq开始拉取消息，注意的是返回的消息从seq+1开始返回，seq为之前接口返回的最大seq值。首次使用请使用seq:0
     * @param limit    一次拉取的消息条数，最大值1000条，超过1000条会返回错误
     * @param proxy    使用代理的请求，需要传入代理的链接。如：socks5://10.0.0.1:8081 或者 http://10.0.0.1:8081
     * @param passwd   代理账号密码，需要传入代理的账号密码。如 user_name:passwd_123
     * @param chatData 返回本次拉取消息的数据，slice结构体.内容包括errcode/errmsg，以及每条消息内容。
     * @return 返回是否调用成功
     * 0   - 成功
     * !=0 - 失败
     */
    public native static int GetChatData(long sdk, long seq, long limit, String proxy, String passwd, long timeout, long chatData);

    /**
     * 拉取媒体消息函数
     * Return值=0表示该API调用成功
     *
     * @param sdk       NewSdk返回的sdk指针
     * @param sdkField  从GetChatData返回的聊天消息中，媒体消息包括的sdkfileid
     * @param proxy     使用代理的请求，需要传入代理的链接。如：socks5://10.0.0.1:8081 或者 http://10.0.0.1:8081
     * @param passwd    代理账号密码，需要传入代理的账号密码。如 user_name:passwd_123
     * @param indexbuf  媒体消息分片拉取，需要填入每次拉取的索引信息。首次不需要填写，默认拉取512k，后续每次调用只需要将上次调用返回的outindexbuf填入即可。
     * @param mediaData 返回本次拉取的媒体数据.MediaData结构体.内容包括data(数据内容)/outindexbuf(下次索引)/is_finish(拉取完成标记)
     * @return 返回是否调用成功
     * 0   - 成功
     * !=0 - 失败
     */
    public native static int GetMediaData(long sdk, String indexbuf, String sdkField, String proxy, String passwd, long timeout, long mediaData);

    /**
     * @param encrypt_key, getchatdata返回的encrypt_key
     * @param encrypt_msg, getchatdata返回的content
     * @param msg,         解密的消息明文
     * @return 返回是否调用成功
     * 0   - 成功
     * !=0 - 失败
     * @brief 解析密文
     */
    public native static int DecryptData(long sdk, String encrypt_key, String encrypt_msg, long msg);

    public native static void DestroySdk(long sdk);

    public native static long NewSlice();

    /**
     * @return
     * @brief 释放slice，和NewSlice成对使用
     */
    public native static void FreeSlice(long slice);

    /**
     * @return 内容
     * @brief 获取slice内容
     */
    public native static String GetContentFromSlice(long slice);

    /**
     * @return 内容
     * @brief 获取slice内容长度
     */
    public native static int GetSliceLen(long slice);

    public native static long NewMediaData();

    public native static void FreeMediaData(long mediaData);

    /**
     * @return outindex
     * @brief 获取mediadata outindex
     */
    public native static String GetOutIndexBuf(long mediaData);

    /**
     * @return data
     * @brief 获取mediadata data数据
     */
    public native static byte[] GetData(long mediaData);

    public native static int GetIndexLen(long mediaData);

    public native static int GetDataLen(long mediaData);

    /**
     * @return 1完成、0未完成
     * @brief 判断mediadata是否结束
     */
    public native static int IsMediaDataFinish(long mediaData);

    /**
     * 参考：https://developers.weixin.qq.com/community/develop/article/doc/000c4655684510e8db2a35d4656813
     */
    static {
        try {
            if (isWindows()) {
                loadLib("libeay32", DllSuffixEnum.WINDOWS.getVaule());
                loadLib("libprotobuf", DllSuffixEnum.WINDOWS.getVaule());
                loadLib("ssleay32", DllSuffixEnum.WINDOWS.getVaule());
                loadLib("libcurl", DllSuffixEnum.WINDOWS.getVaule());
                loadLib("WeWorkFinanceSdk", DllSuffixEnum.WINDOWS.getVaule());
            } else {
                loadLib("libWeWorkFinanceSdk_Java", DllSuffixEnum.LINUX.getVaule());
            }
            log.info("==========>>初始化企业微信会话存档sdk成功");
        } catch (Exception e) {
            log.error("==========>>初始化企业微信会话存档sdk失败！",e);
            throw new RuntimeException(e);
        }
    }

    public static boolean isWindows() {
        String osName = System.getProperties().getProperty("os.name");
        return osName.toUpperCase().indexOf("WINDOWS") != -1;
    }

    private synchronized static void loadLib(String dllName, String suffix) throws IOException {
        String dllFullName = dllName + suffix;
        //缓存路径
        String nativeTempDir = System.getProperty("java.io.tmpdir");
        InputStream in = null;
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;

        File cacheDllFile = new File(nativeTempDir + File.separator + dllFullName);
        if (!cacheDllFile.exists()) {
            try {
                in = Finance.class.getResourceAsStream("com\\com.tencent\\wework\\" + dllFullName);
                if (in == null) {
                    in = Finance.class.getResourceAsStream(dllFullName);
                }
                reader = new BufferedInputStream(in);
                writer = new BufferedOutputStream(new FileOutputStream(cacheDllFile));
                byte[] buffer = new byte[1024];
                while (reader.read(buffer) > 0) {
                    writer.write(buffer);
                    buffer = new byte[1024];
                }
            } catch (Exception e) {
                log.error("==========>>dll文件处理异常！",e);
                throw new RuntimeException(e);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }
        System.load(cacheDllFile.toString());
        log.info("==========>>{}加载成功",cacheDllFile.toString());
    }

}
