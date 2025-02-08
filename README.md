# Receipt Processor API

This is a Spring Boot application that processes receipts and calculates reward points based on predefined rules. 
The logic is defined here https://github.com/fetch-rewards/receipt-processor-challenge/tree/main

## Prerequisites

Before running the project, ensure you have the following installed:

- **Docker** (for containerized deployment)

## How to Run the Project

### 1. Clone the Repository
```sh
git clone https://github.com/mmtbz/receipt-processor-challenge.git
```

### 2. Navigate to the Project Directory
```sh
cd receipt-processor-challenge
```

### 3. Build the Project
```shell
./gradlew clean build
```

### 4. Build the Docker Image
```shell
docker build -t receipt-processor-challenge .
```

### 5. Run the Docker Container
```shell
docker run -p 8080:8080 receipt-processor-challenge
```

### 6. Access the API
Once the container is running, the API will be accessible at:
http://localhost:8080

## Running Unit Tests

Unit tests are located inside the `src/test/` folder.
To run the tests, execute the following command:

```shell
./gradlew test
```

## API Endpoints

### 1. Process a Receipt

- Endpoint: POST `/receipts/process`
- Description: Processes a receipt and returns a unique receipt ID.
- Request Body:
```json
{
  "retailer": "M&M Corner Market",
  "purchaseDate": "2022-03-20",
  "purchaseTime": "14:33",
  "items": [
    {
      "shortDescription": "Gatorade",
      "price": "2.25"
    },
    {
      "shortDescription": "Gatorade",
      "price": "2.25"
    },
    {
      "shortDescription": "Gatorade",
      "price": "2.25"
    },
    {
      "shortDescription": "Gatorade",
      "price": "2.25"
    }
  ],
  "total": "9.00"
}

```
- Example cURL Request:
```shell
curl --location 'http://localhost:8080/receipts/process' \
--header 'Content-Type: application/json' \
--data '{
  "retailer": "M&M Corner Market",
  "purchaseDate": "2022-03-20",
  "purchaseTime": "14:33",
  "items": [
    {
      "shortDescription": "Gatorade",
      "price": "2.25"
    },{
      "shortDescription": "Gatorade",
      "price": "2.25"
    },{
      "shortDescription": "Gatorade",
      "price": "2.25"
    },{
      "shortDescription": "Gatorade",
      "price": "2.25"
    }
  ],
  "total": "9.00"
}'

```

- Response Example:
```json
{
  "id": "cc916c6c-3836-491e-b09d-c3d29fd3fc10"
}

```

### 2. Retrieve Points for a Receipt

- Endpoint: GET `/receipts/{id}/points`
- Description: Retrieves the points earned for a specific receipt.
- Path Parameter:
   `id` (UUID) – The unique receipt ID returned from `/receipts/process`
- Example cURL Request:
```shell
curl --location 'http://localhost:8080/receipts/cc916c6c-3836-491e-b09d-c3d29fd3fc10/points'
```
- Response Example:
```shell
{
  "points": 28
}
```
