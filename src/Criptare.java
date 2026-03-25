import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.io.InputStream;

public class Criptare{
    public String calculeazaHash(Path file){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while((bytesRead = is.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                }
            }

            StringBuilder hexString = new StringBuilder();
            for(byte b : digest.digest()){
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        }catch(Exception e){
            return "Eroare hash: " + e.getMessage();
        }
    }
}