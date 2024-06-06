# Face Recognition Project

© 2024 Vinodh

## Description
This project is a face recognition application developed using JSP in Eclipse IDE, leveraging OpenCV for face detection and recognition. The primary use case demonstrated is face recognition for login purposes, but the application can be extended to other scenarios requiring face recognition.

## Features
- Face recognition-based login system
- Developed using JSP in Eclipse IDE
- Utilizes OpenCV for accurate face detection and recognition
- Extensible for various face recognition applications

## Installation Instructions

### Prerequisites
- **Eclipse IDE**: Ensure you have Eclipse IDE installed on your system.
- **Apache Tomcat**: Ensure you have Apache Tomcat or any other JSP server set up.
- **OpenCV**: Install OpenCV for Java.

### Steps
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/vinodh1299/Face-Recognition.git
   cd Face-Recognition

2. **Open Project in Eclipse:**
   
- Open Eclipse IDE.
- Import the project by selecting File -> Import -> Existing Projects into Workspace -> Select root directory -> Browse to the cloned repository.
- Ensure all dependencies are resolved.

3. **Configure Apache Tomcat:**

- In Eclipse, go to Window -> Preferences -> Server -> Runtime Environments -> Add.
- Select Apache Tomcat and configure it to point to your Tomcat installation directory

4. **Add OpenCV Library:**
   
- Download OpenCV for Java from the OpenCV official website.
- Extract the archive and add the opencv-xxx.jar (replace xxx with the version number) to your project’s build path:
- Right-click on your project in Eclipse -> Build Path -> Configure Build Path -> Libraries -> Add External JARs.
- Copy the native libraries (.dll, .so, or .dylib) to a directory included in your system's PATH.

5. **Deploy the Project:**

- Right-click on your project in Eclipse -> Run As -> Run on Server.
- Select Apache Tomcat and click Finish.

### Usage Instructions

1. **Start Apache Tomcat:**
- Ensure your Apache Tomcat server is running.

2. **Access the Application:**
Open your web browser and navigate to http://localhost:8080/FaceRecognition.

3. **Face Recognition Login:**
Follow the on-screen instructions to use the face recognition feature for login.


### Contact Information
If you have any questions or suggestions, feel free to reach out to us at **vinodhanu007@gmail.com.**
