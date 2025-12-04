# 🚀 BuzzLink AWS Deployment Checklist

Use this checklist to deploy BuzzLink to AWS EC2 using the automated Terraform infrastructure.

## ✅ Pre-Deployment Checklist

### 1. Install Required Tools

- [ ] **Terraform** - `brew install terraform` (macOS) or download from [terraform.io](https://terraform.io)
  ```bash
  terraform --version  # Should show v1.0 or higher
  ```

- [ ] **AWS CLI** - Install and configure
  ```bash
  aws configure
  # Enter your AWS Access Key ID
  # Enter your AWS Secret Access Key
  # Enter region: us-east-1
  # Enter output format: json
  ```

- [ ] **SSH Key** - Generate if you don't have one
  ```bash
  # Check if you have a key
  ls -la ~/.ssh/id_rsa.pub

  # If not, generate one
  ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
  ```

### 2. Prepare Your Information

Gather these before starting:

- [ ] **Your Public IP Address**
  - Visit [whatismyip.com](https://www.whatismyip.com)
  - Write it down: `__________________`

- [ ] **GitHub Repository URL**
  - Example: `https://github.com/yourusername/BuzzLink.git`
  - Your URL: `__________________`
  - Make sure your code is pushed to GitHub!

- [ ] **Clerk Publishable Key**
  - Get from [Clerk Dashboard](https://dashboard.clerk.com) → API Keys
  - Starts with `pk_test_` or `pk_live_`
  - Your key: `__________________`

- [ ] **Database Password**
  - Choose a secure password (at least 12 characters)
  - Your password: `__________________`

## 🎯 Deployment Steps

### Step 1: Configure Terraform

Run the interactive setup script:

```bash
./scripts/setup-terraform.sh
```

This will ask you for all the information above and create `terraform/terraform.tfvars`.

**OR** manually create the file:

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
nano terraform.tfvars  # Edit with your values
```

### Step 2: Deploy to AWS

Run the automated deployment:

```bash
./scripts/terraform-deploy.sh
```

This will:
1. Initialize Terraform
2. Validate configuration
3. Show you what will be created
4. Ask for confirmation
5. Deploy everything to AWS

**Time required:** 3-5 minutes

### Step 3: Save Your Outputs

After deployment, save these URLs:

```
Frontend URL: http://___________________:3000
Backend URL:  http://___________________:8080
SSH Command:  ssh -i ~/.ssh/id_rsa ubuntu@___________________
```

## 🔧 Post-Deployment Configuration

### 1. Update Clerk Redirect URLs

- [ ] Go to [Clerk Dashboard](https://dashboard.clerk.com)
- [ ] Navigate to: **Configure** → **Paths**
- [ ] Add these URLs:
  ```
  http://YOUR_IP:3000
  http://YOUR_IP:3000/sso-callback
  ```

### 2. Set Up Admin User

- [ ] SSH into your instance:
  ```bash
  ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP
  ```

- [ ] Get your Clerk User ID:
  - Log into your app at `http://YOUR_IP:3000`
  - Open browser console
  - Run: `window.Clerk.user.id`
  - Copy your Clerk ID

- [ ] Set yourself as admin:
  ```bash
  docker exec -it buzzlink-postgres psql -U buzzlink_user -d buzzlink

  UPDATE users SET is_admin = true WHERE clerk_id = 'your_clerk_id_here';

  \q
  exit
  ```

### 3. Verify Everything Works

- [ ] **Frontend loads:** Visit `http://YOUR_IP:3000`
- [ ] **Can sign in:** Use Clerk authentication
- [ ] **Can create workspace:** Create a test workspace
- [ ] **Can create channel:** Create a test channel
- [ ] **Can send messages:** Send a test message
- [ ] **Admin dashboard works:** Visit `http://YOUR_IP:3000/admin`
- [ ] **Real-time updates work:** Open in two browsers, send message

## 📊 Monitoring & Maintenance

### Check Application Status

```bash
# SSH into instance
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP

# Check all containers
docker ps

# View logs
docker-compose logs -f

# Restart services
docker-compose restart
```

### Update Application Code

```bash
# SSH into instance
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP

# Pull latest code
cd /home/ubuntu/BuzzLink
git pull origin main

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

## 🐛 Troubleshooting

### Application Not Loading

1. **Check if containers are running:**
   ```bash
   ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP
   docker ps
   ```

2. **Check logs for errors:**
   ```bash
   docker-compose logs backend
   docker-compose logs frontend
   ```

3. **Check user data script:**
   ```bash
   sudo cat /var/log/user-data.log
   ```

### Can't SSH into Instance

1. **Verify your IP hasn't changed:**
   ```bash
   curl https://checkip.amazonaws.com
   ```

2. **If IP changed, update terraform.tfvars and run:**
   ```bash
   cd terraform
   terraform apply
   ```

### Services Won't Start

```bash
# SSH into instance
docker-compose down
docker system prune -f
docker-compose up -d --build
```

## 🗑️ Cleanup (When Done)

To destroy all AWS resources and stop charges:

```bash
cd terraform
terraform destroy
```

Type `yes` to confirm. This will delete:
- EC2 instance
- Elastic IP
- Security groups
- VPC and networking

**Warning:** This is permanent and cannot be undone!

## 💰 Cost Tracking

### Free Tier (First 12 Months)
- ✅ 750 hours/month of t2.micro
- ✅ 30 GB EBS storage
- ✅ 15 GB data transfer out

### After Free Tier
- 💵 ~$8-10/month for t2.micro
- 💵 ~$1-2/month for storage
- 💵 <$1/month for data transfer (typical)

**Total:** FREE for 1 year, then ~$10/month

### Monitor Costs

Check AWS Billing Dashboard:
- [AWS Billing Console](https://console.aws.amazon.com/billing/)

## 📝 Important Notes

### Security Best Practices

- [ ] Change default PostgreSQL password to something secure
- [ ] Restrict SSH access to your IP only (not 0.0.0.0/0)
- [ ] Enable AWS CloudWatch for monitoring
- [ ] Set up billing alerts in AWS
- [ ] Regularly update your EC2 instance:
  ```bash
  sudo apt update && sudo apt upgrade -y
  ```

### Backup Strategy

- [ ] Set up automated database backups
- [ ] Keep your GitHub repository updated
- [ ] Document any manual configuration changes

## ✅ Success Criteria

You've successfully deployed when:

- ✅ Frontend accessible at `http://YOUR_IP:3000`
- ✅ Can sign in with Clerk authentication
- ✅ Can create workspaces and channels
- ✅ Can send and receive messages in real-time
- ✅ Admin dashboard accessible and functional
- ✅ WebSocket connections working (real-time updates)
- ✅ Database persisting data correctly

## 📚 Additional Resources

- **Main Setup Guide:** [INFRASTRUCTURE_SETUP.md](./INFRASTRUCTURE_SETUP.md)
- **Terraform Details:** [terraform/README.md](./terraform/README.md)
- **AWS Documentation:** [docs.aws.amazon.com](https://docs.aws.amazon.com)
- **Terraform Docs:** [terraform.io/docs](https://www.terraform.io/docs)

## 🎉 You're Ready!

Follow this checklist step-by-step, and you'll have BuzzLink running on AWS in less than 10 minutes!

**Questions or issues?** Check the troubleshooting sections in the documentation.
