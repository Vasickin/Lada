# Настройка MySQL для Community CMS

## 1. Установка MySQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server



# Создаем инструкцию по настройке
cat > MYSQL_SETUP.md << 'EOF'
# MySQL Configuration Setup

## 1. Database Setup
```bash
# Connect to MySQL as root
sudo mysql -u root -p

# Create database and user
CREATE DATABASE lada_cms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lada_user'@'localhost' IDENTIFIED BY 'your_secure_password_here';
GRANT ALL PRIVILEGES ON lada_cms.* TO 'lada_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
 mysql -u lada_user -p lada_cms
LadaCMS2025