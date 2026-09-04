import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class RootLook {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  RootLook - Minecraft 权限检测工具");
        System.out.println("========================================");
        System.out.println();

        boolean isRoot = checkRoot();

        if (isRoot) {
            System.out.println("⚠️  当前服务器运行在 ROOT 权限下！");
            System.out.println("   建议：使用普通用户运行 Minecraft 服务端");
            System.out.println("   命令: sudo -u minecraft java -jar server.jar");
        } else {
            System.out.println("✅ 当前服务器未运行在 ROOT 权限下");
            System.out.println("   安全状态：良好");
        }

        System.out.println();
        System.out.println("========================================");
        System.exit(isRoot ? 1 : 0);
    }

    private static boolean checkRoot() {
        // 方法1：检查系统属性 user.name
        String userName = System.getProperty("user.name");
        System.out.println("当前用户: " + userName);
        if ("root".equals(userName)) {
            return true;
        }

        // 方法2：检查 /root 目录是否可写
        try {
            File rootDir = new File("/root");
            if (rootDir.exists() && rootDir.canWrite()) {
                System.out.println("   - 可以写入 /root 目录");
                return true;
            }
        } catch (Exception e) {
            // 忽略
        }

        // 方法3：尝试读取 /etc/shadow（只有 root 可读）
        try {
            Path shadow = Paths.get("/etc/shadow");
            if (Files.exists(shadow)) {
                // 尝试直接读取文件内容
                try {
                    Files.readAllLines(shadow);
                    System.out.println("   - 可以读取 /etc/shadow (敏感文件)");
                    return true;
                } catch (IOException e) {
                    // 读取失败，说明没有 root 权限
                }
            }
        } catch (Exception e) {
            // 忽略
        }

        // 方法4：执行 whoami 命令
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"whoami"});
            String result = new String(process.getInputStream().readAllBytes()).trim();
            System.out.println("   - whoami 结果: " + result);
            if ("root".equals(result)) {
                return true;
            }
        } catch (Exception e) {
            // 忽略
        }

        // 方法5：尝试在 /root 目录创建临时文件
        try {
            File testFile = new File("/root/.rootlook_test");
            if (testFile.createNewFile()) {
                testFile.delete();
                System.out.println("   - 可以在 /root 目录创建文件");
                return true;
            }
        } catch (Exception e) {
            // 忽略
        }

        // 方法6：检查环境变量（有些容器会设置）
        String uid = System.getenv("UID");
        if ("0".equals(uid)) {
            System.out.println("   - 环境变量 UID=0 (root)");
            return true;
        }

        return false;
    }
}
