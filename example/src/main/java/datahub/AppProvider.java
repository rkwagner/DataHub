package datahub;

public class AppProvider {
    public void configureSystem() {
        // Set HADOOP_HOME for Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("hadoop.home.dir", new java.io.File("hadoop").getAbsolutePath());
        }
    }

    public String getBasePath(String tableName) {
        return "s3a://hudi-data/" + tableName;
    }
}
