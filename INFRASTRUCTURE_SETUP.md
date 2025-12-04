# 🚀 BuzzLink Infrastructure Setup Guide

This guide will help you deploy BuzzLink to AWS EC2 using Terraform (Infrastructure as Code).

## ✅ Prerequisites

Before you start, make sure you have:

1. **AWS Account** - [Sign up here](https://aws.amazon.com/free/)
2. **Terraform installed** - [Install guide](https://www.terraform.io/downloads)
3. **AWS CLI configured** - Your AWS credentials set up
4. **SSH key pair** - For accessing the EC2 instance
5. **GitHub repository** - Your BuzzLink code pushed to GitHub

## 📝 Step-by-Step Deployment

### Step 1: Install Terraform

**macOS:**
```bash
brew install terraform
```

**Linux:**
```bash
wget https://releases.hashicorp.com/terraform/1.6.6/terraform_1.6.6_linux_amd64.zip
unzip terraform_1.6.6_linux_amd64.zip
sudo mv terraform /usr/local/bin/
```

**Windows:**
Download from [terraform.io](https://www.terraform.io/downloads) and add to PATH.

Verify installation:
```bash
terraform --version
```

### Step 2: Configure AWS CLI

```bash
aws configure
```

Enter your:
- AWS Access Key ID
- AWS Secret Access Key
- Default region (e.g., `us-east-1`)
- Default output format (e.g., `json`)

Get AWS credentials from: [AWS IAM Console](https://console.aws.amazon.com/iam/)

### Step 3: Generate SSH Key (if you don't have one)

```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
```

Press Enter for all prompts (use default values).

### Step 4: Configure Terraform Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your actual values:

```hcl
# AWS Configuration
aws_region    = "us-east-1"
project_name  = "buzzlink"
instance_type = "t2.micro"

# SSH Configuration
public_key_path  = "~/.ssh/id_rsa.pub"
allowed_ssh_cidr = ["YOUR_IP_ADDRESS/32"]  # Get from https://whatismyip.com

# Application Configuration
github_repo = "https://github.com/YOUR_USERNAME/BuzzLink.git"  # Your repo URL

# Clerk Configuration
clerk_publishable_key = "pk_test_xxxxx"  # From Clerk Dashboard

# Database Configuration
postgres_password = "YourSecurePassword123!"  # Change this!
```

**Important:**
- Replace `YOUR_IP_ADDRESS` with your actual IP from [whatismyip.com](https://www.whatismyip.com)
- Replace `YOUR_USERNAME` with your GitHub username
- Get your Clerk key from [Clerk Dashboard](https://dashboard.clerk.com)

### Step 5: Deploy Infrastructure

#### Option A: Automated Script (Recommended)

```bash
cd ..
chmod +x scripts/terraform-deploy.sh
./scripts/terraform-deploy.sh
```

#### Option B: Manual Commands

```bash
cd terraform

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy infrastructure
terraform apply
```

Type `yes` when prompted.

### Step 6: Wait for Deployment

The deployment takes **3-5 minutes**. The EC2 instance will automatically:
- ✅ Install Docker and Docker Compose
- ✅ Clone your GitHub repository
- ✅ Set up environment files
- ✅ Build and start all containers

### Step 7: Get Your URLs

After deployment, you'll see:

```
Outputs:

instance_public_ip = "54.123.45.67"
frontend_url = "http://54.123.45.67:3000"
backend_url = "http://54.123.45.67:8080"
ssh_command = "ssh -i ~/.ssh/id_rsa ubuntu@54.123.45.67"
```

### Step 8: Access Your Application

Open your browser and go to the frontend URL!

**Example:** `http://54.123.45.67:3000`

## 🔧 Post-Deployment Tasks

### Update Clerk Redirect URLs

1. Go to [Clerk Dashboard](https://dashboard.clerk.com)
2. Navigate to **API Keys** → **Redirect URLs**
3. Add your new URLs:
   ```
   http://YOUR_IP:3000
   http://YOUR_IP:3000/sso-callback
   ```

### Set Up Your Admin User

SSH into the instance and set yourself as admin:

```bash
# SSH into instance
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP

# Access PostgreSQL
docker exec -it buzzlink-postgres psql -U buzzlink_user -d buzzlink

# Set yourself as admin (replace with your Clerk ID)
UPDATE users SET is_admin = true WHERE clerk_id = 'your_clerk_user_id';

# Exit PostgreSQL
\q

# Exit SSH
exit
```

## 📊 Useful Commands

### View Terraform Outputs

```bash
cd terraform
terraform output
terraform output -raw instance_public_ip
```

### SSH into Your Instance

```bash
ssh -i ~/.ssh/id_rsa ubuntu@$(cd terraform && terraform output -raw instance_public_ip)
```

### Check Application Status

```bash
# SSH into instance first
docker ps
docker-compose ps
docker-compose logs -f
```

### View Application Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f frontend
docker-compose logs -f backend
docker-compose logs -f postgres
```

### Restart Services

```bash
docker-compose restart
```

### Update Application Code

```bash
cd /home/ubuntu/BuzzLink
git pull origin main
docker-compose down
docker-compose up -d --build
```

## 🐛 Troubleshooting

### Application Not Loading

1. **Check if services are running:**
   ```bash
   ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP
   docker ps
   ```

2. **View logs:**
   ```bash
   docker-compose logs backend
   docker-compose logs frontend
   ```

3. **Check user data execution:**
   ```bash
   sudo cat /var/log/user-data.log
   ```

### Can't SSH into Instance

1. **Verify your IP hasn't changed:**
   ```bash
   curl https://checkip.amazonaws.com
   ```

2. **Update security group:**
   Edit `terraform.tfvars` with new IP and run:
   ```bash
   terraform apply
   ```

### Port Already in Use

```bash
docker-compose down
docker ps -a  # Check for any lingering containers
docker-compose up -d
```

### Database Connection Issues

```bash
# Check PostgreSQL logs
docker logs buzzlink-postgres

# Restart backend
docker-compose restart backend
```

## 🗑️ Destroy Infrastructure

**Warning:** This will delete everything!

```bash
cd terraform
terraform destroy
```

Type `yes` to confirm.

## 💰 Cost Estimate

With **t2.micro** (Free Tier eligible):

- **First 12 months:** FREE (750 hours/month)
- **After free tier:** ~$8-10/month
- **Storage:** $1-2/month (20GB EBS)
- **Data transfer:** Usually <$1/month for demo

**Total:** $0/month (free tier) or ~$10/month after

## 🎯 What Gets Deployed

Your Terraform setup creates:

1. **VPC** - Virtual Private Cloud
2. **Public Subnet** - For your EC2 instance
3. **Internet Gateway** - For internet access
4. **Security Group** - Firewall rules for ports 22, 80, 443, 3000, 8080, 5432
5. **EC2 Instance** - t2.micro running Ubuntu 22.04
6. **Elastic IP** - Persistent public IP address
7. **Automated Setup** - Installs Docker, clones repo, starts app

## 📚 Additional Resources

- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS Free Tier](https://aws.amazon.com/free/)
- [Docker Documentation](https://docs.docker.com/)
- [Detailed Terraform README](./terraform/README.md)

## ✅ Success Checklist

- [ ] Terraform installed and working
- [ ] AWS CLI configured with credentials
- [ ] SSH key generated (`~/.ssh/id_rsa.pub` exists)
- [ ] GitHub repository is public or you have deploy keys
- [ ] `terraform.tfvars` filled with correct values
- [ ] Deployment completed successfully
- [ ] Can access frontend at `http://YOUR_IP:3000`
- [ ] Clerk redirect URLs updated
- [ ] Admin user set up in database

## 🎉 You're Done!

Your BuzzLink application is now running on AWS with fully automated infrastructure!

**Frontend:** http://YOUR_IP:3000
**Backend:** http://YOUR_IP:8080

Need help? Check the [Terraform README](./terraform/README.md) for detailed troubleshooting.
