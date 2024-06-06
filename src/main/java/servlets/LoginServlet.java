package servlets;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.MatOfRect;
import utils.PasswordUtil;

@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static CascadeClassifier faceDetector;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();
        String haarCascadePath = context.getRealPath("/WEB-INF/haarcascade_frontalface_default.xml");
        faceDetector = new CascadeClassifier(haarCascadePath);
        if (faceDetector.empty()) {
            throw new IllegalStateException("Failed to load Haar Cascade XML file.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer failedAttempts = (Integer) session.getAttribute("failedAttempts");
        if (failedAttempts == null) {
            failedAttempts = 0;
        }

        String faceImage = request.getParameter("faceImage");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LOGGER.info("Login attempt with username: " + username);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Username and password login
            if (authenticateWithUsernameAndPassword(username, password, session)) {
                LOGGER.info("Login successful for username: " + username);
                response.sendRedirect("Test.jsp");
                session.setAttribute("failedAttempts", 0); // Reset failed attempts after successful login
            } else {
                LOGGER.warning("Login failed for username: " + username);
                session.setAttribute("failedAttempts", ++failedAttempts);
                request.setAttribute("errorMessage", "Invalid username or password.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        } else {
            // Face recognition login
            if (faceImage == null || faceImage.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Face image not captured.");
                return;
            }

            try {
                byte[] imageBytes = Base64.getDecoder().decode(faceImage.split(",")[1]);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage bufferedImage = ImageIO.read(bis);
                Mat capturedFaceMat = bufferedImageToMat(bufferedImage);

                // Detect face in the captured image
                Mat faceMat = detectFace(capturedFaceMat);
                if (faceMat.empty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No face detected in the captured image.");
                    return;
                }

                Imgproc.resize(faceMat, faceMat, new Size(100, 100)); // Resize captured face

                String empName = authenticateWithFace(faceMat, session);
                if (empName != null) {
                    LOGGER.info("Face recognition login successful for username: " + empName);
                    response.sendRedirect("Test.jsp");
                    session.setAttribute("failedAttempts", 0); // Reset failed attempts after successful login
                } else {
                    LOGGER.warning("Face recognition login failed for username: " + username);
                    session.setAttribute("failedAttempts", ++failedAttempts);
                    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                        request.setAttribute("errorMessage", "Face recognition failed multiple times. Please use username and password to login.");
                    } else {
                        request.setAttribute("errorMessage", "Face recognition failed. Please try again.");
                    }
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                }
            } catch (IOException | SQLException e) {
                LOGGER.log(Level.SEVERE, "An error occurred during face recognition.", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred during face recognition.");
            }
        }
    }

    private String authenticateWithFace(Mat capturedFaceMat, HttpSession session) throws SQLException, IOException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT emp_name, emp_no, face_image_data FROM employee_details WHERE face_image_data IS NOT NULL");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Blob blob = rs.getBlob("face_image_data");
                if (blob == null) continue;

                try (InputStream inputStream = blob.getBinaryStream()) {
                    byte[] imageBytes = inputStream.readAllBytes();
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    Mat storedFaceMat = bufferedImageToMat(bufferedImage);

                    // Detect face in the stored image
                    Mat faceMat = detectFace(storedFaceMat);
                    if (faceMat.empty()) continue;

                    Imgproc.resize(faceMat, faceMat, new Size(100, 100)); // Resize stored face

                    double similarity = compareFaces(faceMat, capturedFaceMat);
                    LOGGER.info("Face similarity: " + similarity);

                    if (similarity >= 30) {
                        String empName = rs.getString("emp_name");
                        String empNo = rs.getString("emp_no");
                        session.setAttribute("empName", empName);
                        session.setAttribute("empNo", empNo);
                        
                        // Print compared face_image_data
                        byte[] comparedImageData = Base64.getEncoder().encode(imageBytes);
                        String comparedImageDataString = new String(comparedImageData);
                        LOGGER.info("Compared face_image_data: " + comparedImageDataString);
                        
                        return empName;
                    }
                }
            }
        }
        return null;
    }

    private boolean authenticateWithUsernameAndPassword(String username, String password, HttpSession session) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT emp_name, emp_no, password FROM employee_details WHERE emp_name = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (PasswordUtil.verifyPassword(password, storedPassword)) {
                    String empName = rs.getString("emp_name");
                    String empNo = rs.getString("emp_no");
                    session.setAttribute("empName", empName);
                    session.setAttribute("empNo", empNo);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "An error occurred while authenticating with username and password.", e);
        }
        return false;
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private Mat detectFace(Mat image) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        for (Rect rect : faceDetections.toArray()) {
            return new Mat(image, rect);
        }
        return new Mat(); // Return an empty Mat if no face is detected
    }

    private double compareFaces(Mat storedFace, Mat capturedFace) {
        // Convert to grayscale if needed
        if (storedFace.channels() > 1) {
            Imgproc.cvtColor(storedFace, storedFace, Imgproc.COLOR_BGR2GRAY);
        }
        if (capturedFace.channels() > 1) {
            Imgproc.cvtColor(capturedFace, capturedFace, Imgproc.COLOR_BGR2GRAY);
        }

        // Resize to the same size
        if (!storedFace.size().equals(capturedFace.size())) {
            Imgproc.resize(capturedFace, capturedFace, storedFace.size());
        }

        // Compare faces (simple method)
        Mat diff = new Mat();
        Core.absdiff(storedFace, capturedFace, diff);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(diff);

        double similarity = 100 - (mmr.maxVal * 100 / 255);
        return similarity; // Return the similarity score
    }
}
