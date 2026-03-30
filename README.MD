# Integrate E-Commerce Project with VNPay and MOMO

This is a simple e-commerce project that demonstrates how to integrate third-party payment gateways such as **VNPay** and **MOMO**. The project is designed to be extendable with additional payment providers in the future.

## Features

* 🛒 Basic shopping flow with select product variants, create orders, and checkout
* 💳 Payment gateway integration with:
    * VNPay 
    * MOMO
* 📦 Order creation and confirmation
* 🔑 Support Authentication
## Documentation Links

* [VNPay Official Sandbox Docs](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html)
* 

CÁC BƯỚC CHẠY CHƯƠNG TRÌNH:
1. Chỉnh sửa các cấu hình trong file .env cho phù hợp với máy
2. Load .env:

Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
    }
}

2. Chạy chương trình:
.\mvnw spring-boot:run

3. Truy cập web: http://localhost:8080/index.html
http://localhost:8080