# 🎯 What You Need to Do - Quick Summary

Everything has been set up! Here's what **YOU** need to do to deploy BuzzLink to AWS.

## 📋 Before You Start

Make sure you have:
1. ✅ Your code pushed to GitHub
2. ✅ AWS account created
3. ✅ Clerk account and keys

## 🚀 Deployment Steps (5 Simple Steps)

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

Verify:
```bash
terraform --version
```

### Step 2: Configure AWS

```bash
aws configure
```

Enter your AWS credentials (get from AWS IAM Console).

### Step 3: Generate SSH Key (if you don't have one)

```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
```

### Step 4: Run Setup Script

```bash
./scripts/setup-terraform.sh
```

This will ask you for:
- Your IP address (it will detect it automatically)
- Your GitHub repo URL
- Your Clerk publishable key
- A PostgreSQL password

### Step 5: Deploy!

```bash
./scripts/terraform-deploy.sh
```

This will:
1. Show you what will be created
2. Ask for confirmation (type `yes`)
3. Create everything on AWS
4. Give you the URLs to access your app

**Wait 3-5 minutes** for the app to fully start.

## 🎉 That's It!

You'll get URLs like:
```
Frontend: http://54.123.45.67:3000
Backend:  http://54.123.45.67:8080
```

## 🔧 After Deployment

### 1. Update Clerk

Go to [Clerk Dashboard](https://dashboard.clerk.com) → Configure → Paths

Add:
```
http://YOUR_IP:3000
http://YOUR_IP:3000/sso-callback
```

### 2. Set Yourself as Admin

```bash
# SSH into instance
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP

# Set admin
docker exec -it buzzlink-postgres psql -U buzzlink_user -d buzzlink

UPDATE users SET is_admin = true WHERE clerk_id = 'your_clerk_id_here';

\q
exit
```

Get your Clerk ID from browser console after logging in:
```javascript
window.Clerk.user.id
```

## 📚 Need More Details?

See these files:
- **[DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md)** - Detailed step-by-step
- **[INFRASTRUCTURE_SETUP.md](./INFRASTRUCTURE_SETUP.md)** - Full guide
- **[terraform/README.md](./terraform/README.md)** - Terraform details

## 🐛 Something Wrong?

Check the troubleshooting sections in the documentation above.

## 💰 Cost

- **First 12 months:** FREE (AWS Free Tier)
- **After:** ~$10/month

## ⚠️ When You're Done

To delete everything and stop charges:

```bash
cd terraform
terraform destroy
```

---

**That's literally it!** Just 5 commands and you're deployed to AWS! 🚀
