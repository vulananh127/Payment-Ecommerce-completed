# Integrate E-Commerce Project with VNPay and Stripe

This is a simple e-commerce project that demonstrates how to integrate third-party payment gateways such as **VNPay** and **Stripe**. The project is designed to be extendable with additional payment providers in the future.

## Features

* 🛒 Basic shopping flow with select product variants, create orders, and checkout
* 💳 Payment gateway integration with:
    * VNPay 
    * Stripe
* 📦 Order creation and confirmation
* 🔑 Support Authentication
## Documentation Links

* [VNPay Official Sandbox Docs](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html)
* [Stripe API Documentation](https://docs.stripe.com/api)

Lỗi thường gặp:

❌ Cannot connect
SQL Server chưa bật
sai port
chưa enable TCP/IP
FIX:
1. Mở SQL Server Configuration Manager
2. Vào SQL Server Network Configuration → Protocols for MSSQLSERVER
3. Click chuột phải TCP/IP → Enable
4. Restart SQL Server service

❌ Login failed for user 'sa'
sai password
chưa bật SQL Server Authentication
FIX: 
1. vào ssms 
2. Reset password sa
chạy câu lệnh sql dưới đây:
ALTER LOGIN sa WITH PASSWORD = '123456';
ALTER LOGIN sa ENABLE;
3. Bật SQL Server Authentication
Trong SSMS:
Click chuột phải server -> Properties -> Security -> Chọn: SQL Server and Windows Authentication mode -> OK
Click chuột phải server -> Restart SQL Server

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