<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Registration with Face Recognition</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f9;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            box-sizing: border-box;
        }

        .container {
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            padding: 20px;
            max-width: 400px;
            width: 100%;
            box-sizing: border-box;
            margin-top: 10%;
        }

        h2 {
            text-align: center;
            color: #333;
        }

        #camera-container {
            margin-bottom: 10px;
            text-align: center;
                width: 70%;
    margin-left: 46px;
    padding: 10px 10px 6px 10px;
    box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);
        }

        #video {
            border-radius: 8px;
            max-width: 100%;
            height: auto;
        }

        #feedback {
            color: green;
            font-weight: bold;
            text-align: center;
            margin-bottom: 10px;
        }

        .error {
            color: red;
        }

        form {
            display: flex;
            flex-direction: column;
        }

        label {
            margin: 7px 0 5px;
            color: #333;
        }

        input[type="text"],
        input[type="number"],
        input[type="password"] {
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
            margin-bottom: 10px;
            box-sizing: border-box;
            width: 100%;
        }

        button {
            padding: 10px;
            border: none;
            border-radius: 4px;
            background-color: #007bff;
            color: white;
            font-size: 16px;
            cursor: pointer;
            margin-bottom: 10px;
        }

        button:disabled {
            background-color: gray;
            cursor: not-allowed;
        }

        @media (max-width: 480px) {
            .container {
                padding: 15px;
            }

            input[type="text"],
            input[type="number"],
            input[type="password"] {
                padding: 8px;
            }

            button {
                padding: 8px;
                font-size: 14px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div id="camera-container">
            <video id="video" width="320" height="240" autoplay></video>
            <canvas id="canvas" style="display:none;"></canvas>
        </div>
        
                    
        <div id="feedback"></div>
        
        <form id="registration-form" action="registerServlet" method="post">
        <button type="button" id="capture-btn" disabled>Capture</button>
            <label for="emp-name">EMP NAME:</label>
            <input type="text" id="username" name="username" required>
            
            <label for="emp-no">EMP NO:</label>
            <input type="number" id="emp-no" name="emp-no" required>
            
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
            
            <input type="hidden" id="faceImageData" name="faceImageData">
            

            <button type="submit" id="submit-btn">Register</button>
        </form>
    </div>
    <script>
    document.addEventListener("DOMContentLoaded", function() {
        const video = document.getElementById('video');
        const canvas = document.getElementById('canvas');
        const faceImageDataInput = document.getElementById('faceImageData');
        const feedback = document.getElementById('feedback');
        const captureBtn = document.getElementById('capture-btn');
        const submitBtn = document.getElementById('submit-btn');

        navigator.mediaDevices.getUserMedia({ video: true })
            .then(stream => {
                video.srcObject = stream;
                video.onloadedmetadata = () => {
                    video.play();
                    captureBtn.disabled = false;
                    feedback.textContent = "Camera is ready.";
                    feedback.classList.remove('error');
                    
                    // Dynamically set canvas dimensions to match video dimensions
                    canvas.width = video.videoWidth;
                    canvas.height = video.videoHeight;
                };
            })
            .catch(error => {
                console.error('Error accessing camera:', error);
                feedback.textContent = "Error accessing camera. Please check your camera settings or permissions.";
                feedback.classList.add('error');
            });

        captureBtn.addEventListener('click', () => {
            const context = canvas.getContext('2d');
            context.drawImage(video, 0, 0, canvas.width, canvas.height);
            const image = canvas.toDataURL('image/jpeg');
            faceImageDataInput.value = image.split(',')[1]; // Extract base64 part
            feedback.textContent = "Image captured successfully.";
            feedback.classList.remove('error');
            console.log("Captured image data:", faceImageDataInput.value);
        });

        submitBtn.addEventListener('click', function(event) {
            if (!faceImageDataInput.value) {
                event.preventDefault();
                feedback.textContent = "Please capture an image before submitting.";
                feedback.classList.add('error');
            }
        });
    });

    </script>
</body>
</html>
