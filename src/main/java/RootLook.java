import java.io.File;
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

        // 退出码：0=安全，1=危险
        System.exit(isRoot ? 1 : 0);
    }

    private static boolean checkRoot() {
        // 方法1：检查系统属性 user.name
        String userName = System.getProperty("user.name");
        System.out.println("当前用户: " + userName);
        if ("root".equals(userName)) {
            return true;
        }

        // 方法2：检查是否可以用 root 权限写 /root 目录
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
                // 检查 POSIX 权限
                if (Files.getFileStore(shadow).supportsFileAttributeView(PosixFileAttributes.class)) {
                    PosixFileAttributes attrs = Files.readAttributes(shadow, PosixFileAttributes.class);
                    Set<PosixFilePermission> perms = attrs.permissions();
                    // 如果当前用户能读 /etc/shadow，大概率是 root
                    if (perms.contains(PosixFilePermission.OWNER_READ)) {
                        // 尝试实际读取
                        try {
                            Files.readAllLines(shadow);
                            System.out.println("   - 可以读取 /etc/shadow (敏感文件)");
                            return true;
                        } catch (Exception e) {
                            // 读不了，说明不是 root
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }

        // 方法4：执行 whoami 命令检查
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

        return false;
    }
}
