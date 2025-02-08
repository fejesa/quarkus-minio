#!/bin/sh
set -e

# Wait for MinIO to be ready
sleep 5

# Configure the MinIO client (mc)
CUSTOM_MINIO="custom-minio"
CUSTOM_BUCKET="media"
mc alias set $CUSTOM_MINIO http://minio:9000 minioroot miniopassword

# Define access and secret keys
USER_ACCESS_KEY="media-access-key"
USER_SECRET_KEY="media-secret-key"

# Create the user
mc admin user add $CUSTOM_MINIO $USER_ACCESS_KEY $USER_SECRET_KEY

# Assign the "readwrite" policy to the user
mc admin policy attach $CUSTOM_MINIO readwrite --user=$USER_ACCESS_KEY
echo "User $USER_ACCESS_KEY created and assigned readwrite policy."

# Create the bucket if it doesn't exist
mc mb $CUSTOM_MINIO/$CUSTOM_BUCKET || true
echo "Bucket $CUSTOM_BUCKET creation script completed."
