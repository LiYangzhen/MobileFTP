package com.example.mobileftp.interfaces;

import com.example.mobileftp.Impl.Listener;
import java.io.File;
import java.io.IOException;

public interface ClientInterface {

    /**
     *  每个方法成功时都会返回 "OK {}" {}里面是返回的参数
     *  例如passive方法会返回 "OK 1024"
     *  一般来说成功时参数不用管，只用判断返回值是否是OK开头的就行了
     *  如果方法错误会返回错误信息（用户名输错了等），直接显示到屏幕上即可
     *  如果报异常了，建议直接catch然后将错误信息输出到屏幕上
     */

    //登录，port默认21，IP默认127.0.0.1
    String connect(String username, String password, String IP, int port) throws IOException;

    String quit();

    //指定数据传输的端口，在传文件之前必须调用一下port获passive方法，不然会报错
    String port(int port) throws IOException;

    //被动模式，即让服务器端指定数据传输的接口并返回，例如返回“OK 124”，返回的端口号前端不用管
    String passive() throws IOException;

    //指定传输方式，type必须是ascii或者binary不然会报错 (这个前端不用检验，报错了直接显示错误信息就行了)
    String type(String type) throws IOException;

    //将指定文件储存到服务器的指定路径上，path: 在服务器那边保存的路径，file_path: 要传输的本地文件的路径
    String store(String path, File file, Listener listener) throws IOException;

    //将服务器上的文件复制下来，path: 服务器那边的文件路径，copy_path: 保存到本地的目标路径
    String retrieve(String path, String copy_path, Listener listener) throws IOException;

    //无实际操作，一般返回OK
    String mode(String mode) throws IOException;

    //同上
    String structure(String structure) throws IOException;

    //同上
    String noop() throws IOException;

    String[] getFileList() throws IOException;
}
