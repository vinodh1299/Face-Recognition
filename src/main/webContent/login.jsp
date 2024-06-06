<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <link rel="stylesheet" type="text/css" href="loginstyles.css">
    <style>
    #camera-container{
	width: 78%;
    margin-left: 19px;
    padding: 10px 10px 6px 10px;
    box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);
}
    
    </style>
</head>
<body>
    <form id="loginForm" method="post" action="loginServlet">
        <h2>Login</h2>

        <!-- Error message section -->
        <c:if test="${not empty errorMessage}">
            <div class="error">${errorMessage}</div>
        </c:if>

        <!-- Username and password login section -->
        <div id="usernamePasswordSection" style="display: none;">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password">
            
                    <div class="submit">
            <button type="submit" id="login-btn">Login</button>
        </div>
        </div>

        <!-- Face capture section -->
        <div style="   
    margin-left: 13px;" 
    id="faceRecognitionSection">
    
    
            <div id="camera-container">
    
    
                <video 
                style="
    width: 100%;
    height:215px
    margin-left: 0px;" id="video"  autoplay></video>
                <canvas id="canvas" style="display:none;"></canvas>
                <input type="hidden" id="faceImage" name="faceImage">
            </div>
            <div id="faceRecognitionFeedback"></div>
        </div>
       
        <div>
            <p style="    
            font-size: 13px;
    margin-left: 18px;
    font-weight: 700;">
    If you don't have an account, 
    <a style="text-decoration: none;
    color: #24e024;" href="register.jsp">
    Create Here</a>.</p>
        </div>

    </form>

     <script>
        const video = document.getElementById('video');
        const canvas = document.getElementById('canvas');
        const faceImageInput = document.getElementById('faceImage');
        const loginForm = document.getElementById('loginForm');
        const feedback = document.getElementById('faceRecognitionFeedback');
        const usernamePasswordSection = document.getElementById('usernamePasswordSection');
        const faceRecognitionSection = document.getElementById('faceRecognitionSection');

        let failedAttempts = <%= session.getAttribute("failedAttempts") != null ? session.getAttribute("failedAttempts") : 0 %>;

        if (failedAttempts >= 3) {
            faceRecognitionSection.style.display = 'none';
            usernamePasswordSection.style.display = 'block';
        } else {
            faceRecognitionSection.style.display = 'block';
            usernamePasswordSection.style.display = 'none';

            navigator.mediaDevices.getUserMedia({ video: true })
                .then(stream => {
                    video.srcObject = stream;
                    video.onloadedmetadata = () => {
                        video.play();
                        setTimeout(captureFace, 5000);  // Capture image after 5 seconds
                    };
                })
                .catch(error => {
                    console.error('Error accessing camera:', error);
                    feedback.innerText = "Error accessing camera.";
                });
        }

        function captureFace() {
            const context = canvas.getContext('2d');
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            context.drawImage(video, 0, 0, canvas.width, canvas.height);
            const image = canvas.toDataURL('image/jpeg');
            faceImageInput.value = image;
            console.log("Captured image data:", image);
            feedback.innerText = "Face captured. Submitting form...";
            loginForm.submit();
        }
    </script>
</body>
</html>
