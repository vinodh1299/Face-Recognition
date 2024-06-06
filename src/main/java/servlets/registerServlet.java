package servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte; // Add this import statement
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import utils.PasswordUtil;

@WebServlet("/registerServlet")
public class registerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV library. Make sure the native library is available.");
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String empName = request.getParameter("username");
        String empNo = request.getParameter("emp-no");
        String password = request.getParameter("password");
        String faceImageData = request.getParameter("faceImageData");

        if (empName == null || empNo == null || password == null || faceImageData == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incomplete registration data.");
            return;
        }

        try {
            // Decode the base64 image data
            byte[] decodedImageData = Base64.getDecoder().decode(faceImageData);
            Mat image = Imgcodecs.imdecode(new MatOfByte(decodedImageData), Imgcodecs.IMREAD_COLOR);

            if (image.empty()) {
                throw new IOException("Failed to decode image.");
            }

            // Perform face detection
            String haarcascadePath = getServletContext().getRealPath("/WEB-INF/haarcascade_frontalface_default.xml");
            CascadeClassifier faceDetector = new CascadeClassifier(haarcascadePath);
            if (faceDetector.empty()) {
                throw new IOException("Failed to load Haar Cascade XML file.");
            }

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);

            if (faceDetections.toArray().length == 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No face detected in the image.");
                return;
            }

            // Hash the password
            String hashedPassword = PasswordUtil.hashPassword(password);

            // Save user data along with the original face image data
            if (registerUser(empName, empNo, hashedPassword, decodedImageData)) {
                response.sendRedirect("login.jsp");
            } else {
                response.sendRedirect("register.jsp?error=registration_failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing image: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error during registration: " + e.getMessage());
        }
    }

    private boolean registerUser(String empName, String empNo, String hashedPassword, byte[] faceImageData) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO employee_details (emp_name, emp_no, password, face_image_data) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, empName);
            stmt.setString(2, empNo);
            stmt.setString(3, hashedPassword);
            stmt.setBytes(4, faceImageData);
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        }
    }
}
