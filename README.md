# Quarkus MinIO Media Storage

## Introduction
This project integrates [MinIO](https://min.io/) with [Quarkus](https://quarkus.io/) to provide a scalable and high-performance object storage solution. It is designed to handle large amounts of unstructured data such as photos, videos, audio files, and PDF documents in streaming mode.

## Features
- Integration with Quarkus for rapid application development.
- Uses MinIO for object storage.
- Supports PostgreSQL for database operations.

## Installation
### Prerequisites
- JDK 21 or higher
- Maven 3.5+
- Docker (for running MinIO and PostgreSQL services)

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/fejesa/quarkus-minio.git
    ```
2. Build the project:
   ```sh
   mvn clean install
   ```
3. Run the application in development mode:
   ```sh
    mvn quarkus:dev
    ```

## Configuration
The application can be configured via `application.properties`. The following properties are available:
```properties
# Quarkus HTTP configuration
quarkus.http.port = 8080

# PostgreSQL configuration
quarkus.datasource.devservices.port = 5432
quarkus.datasource.devservices.db-name = media
quarkus.datasource.devservices.username = media
quarkus.datasource.devservices.password = media

# Hibernate ORM configuration
quarkus.hibernate-orm.database.generation = drop-and-create
quarkus.hibernate-orm.log.sql = true

# MinIO configuration
quarkus.minio.devservices.port = 9000
quarkus.minio.devservices.image-name = minio/minio
quarkus.minio.devservices.access-key = minioaccess
quarkus.minio.devservices.secret-key = miniosecret
minio.bucket-name = media

# The maximum allowed size of an HTTP request body.
quarkus.http.limits.max-body-size = 501M
```

## Usage
Once the application is running, you can upload files from command line using for example [httpie](https://httpie.io/) or [curl](https://curl.se/).
The following example demonstrates how to upload the sample files from the project root directory using _httpie_:
   ```sh
   http --verify=no --form POST http://localhost:8080/api \
   media@./src/test/resources/sample-audio.mp3 \
   description='{"checksum":"9a2270d5964f64981fb1e91dd13e5941262817bdce873cf357c92adbef906b5d"}'
   
   http --verify=no --form POST http://localhost:8080/api \
   media@./src/test/resources/sample-image.png \
   description='{"checksum":"aad96d410d92b5589d41e8462507e3af57682022db3d3711a236c0245fcf296e"}'
   
   http --verify=no --form POST http://localhost:8080/api \
   media@./src/test/resources/sample-video.mp4 \
   description='{"checksum":"71944d7430c461f0cd6e7fd10cee7eb72786352a3678fc7bc0ae3d410f72aece"}'
   
   http --verify=no --form POST http://localhost:8080/api \
   media@./src/test/resources/sample-pdf.pdf \
   description='{"checksum":"38c9792d725c45dd431699e6a3b0f0f8e17c63c9ac7331387ee30dcc6e42a511"}'
   ```
where `media` is the form field name for the file and `description` is a JSON object containing checksum information.
To calculate the checksum of a file, you can use the following command:
   ```sh
   sha256sum sample-audio.mp3
   ```
The application will respond with url to access the uploaded file from browser.

### How to view media files
When you navigate to `https://localhost:8080/media?m=xxx`, you will see a sample media file of the specified type. You can check that:
* file cannot be downloaded
* file cannot be accessed directly by URL - open the DevConsole of the browser and try to access the file directly by copying the URL from the Network tab.
* right-click on the file or the page should not work
* try to open the URL from a different tool, for example, Postman, and check if the file is accessible.
* if the file type is PDF then printing the file should not work.
* in case of any error, the user should be redirected to the error page.

**Note**: The list of the uploaded files can be accessed at `http://localhost:8080/api`.

## Starting the Application Using Docker
### Build Docker file
To build the docker image, execute the following commands
```
mvn clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t media-fs .
```
The application depends on MinIO and PostgreSQL services. You can start them using the following commands:
```sh
docker compose -f src/main/docker/services.yml up
```
where `services.yml` is a docker-compose file that starts MinIO, and PostgreSQL services and the application as a container.

* **PostgreSQL Database Service**: Runs a PostgreSQL database container.
  * Port: 5432
  * Data Persistence: ../../../postgres-data
  * Health Check: Ensures PostgreSQL is ready before other services depend on it.
* **MinIO Object Storage Service**: Runs a MinIO container for object storage.
  * Ports: 9000 (API), 9001 (Web UI)
  * Data Persistence: ../../../minio-data
  * Initialization: The minio-init service initializes MinIO, such as creating buckets.
* **Application Service (media-fs)**: Runs the main application container.
  * Port: 8080 (HTTP)
  * Port 10099 (JMX)

**Note**: The application will be available at `http://localhost:8080` and MinIO at `http://localhost:9000`. To access MinIO console, use the user **minioroot** with password **miniopassword**.

## Notes
The application uses [PrimeFaces](https://www.primefaces.org/) and [Primefaces Extensions](https://www.primefaces.org/showcase-ext/views/home.jsf) to display media files.
We use the following components:
* [Image](https://www.primefaces.org/showcase/ui/multimedia/graphicImage.xhtml)
* [Document Viewer](https://www.primefaces.org/showcase-ext/sections/documentviewer/basic.jsf)
* [Video](https://www.primefaces.org/showcase/ui/multimedia/video.xhtml)
* [Audio](https://www.primefaces.org/showcase/ui/multimedia/audio.xhtml)


## Contributing
Contributions are welcome! Please fork the repository and submit pull requests.
