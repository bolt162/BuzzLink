# BuzzLink AWS EC2 Deployment Guide

This guide will help you deploy BuzzLink on AWS EC2 using Docker and PostgreSQL.

## Prerequisites

- AWS Account (Free Tier eligible)
- Clerk account with API keys
- Email account for SMTP (Gmail recommended)
- Your BuzzLink repository pushed to GitHub

## Cost

**100% FREE** - Uses only AWS Free Tier resources:
- 1x t2.micro EC2 instance (750 hours/month free for 12 months)
- 30 GB EBS storage (included in Free Tier)

## Architecture

```
┌─────────────────────────────────────┐
│         EC2 Instance (t2.micro)     │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Docker Compose             │  │
│  │                              │  │
│  │  ┌────────┐  ┌────────────┐ │  │
│  │  │ Postgres│  │  Backend   │ │  │
│  │  │  :5432 │  │   :8080    │ │  │
│  │  └────────┘  └────────────┘ │  │
│  │                              │  │
│  │  ┌────────────┐              │  │
│  │  │  Frontend  │              │  │
│  │  │   :3000    │              │  │
│  │  └────────────┘              │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

## Step 1: Launch EC2 Instance

1. **Go to AWS Console** → EC2 → Launch Instance

2. **Configure Instance**:
   - **Name**: BuzzLink-Server
   - **AMI**: Ubuntu Server 22.04 LTS (Free tier eligible)
   - **Instance Type**: t2.micro (Free tier eligible)
   - **Key Pair**: Create new or use existing (download .pem file)
   - **Network Settings**:
     - Allow SSH (port 22) from your IP
     - Allow HTTP (port 80) from anywhere
     - Allow Custom TCP (port 3000) from anywhere - Frontend
     - Allow Custom TCP (port 8080) from anywhere - Backend
   - **Storage**: 30 GB gp3 (Free tier eligible)

3. **Launch Instance** and wait for it to start

4. **Note your Public IP**: Go to EC2 Dashboard → Select instance → Copy "Public IPv4 address"

## Step 2: Connect to EC2

```bash
# Replace with your key file and public IP
chmod 400 your-key.pem
ssh -i your-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

## Step 3: Setup EC2 Environment

On your EC2 instance, download and run the setup script:

```bash
# Download the setup script
wget https://raw.githubusercontent.com/YOUR_USERNAME/BuzzLink/main/scripts/setup-ec2.sh

# Make it executable
chmod +x setup-ec2.sh

# Run the setup script
./setup-ec2.sh
```

**IMPORTANT**: After the script completes, you MUST log out and log back in:

```bash
# Log out
exit

# Log back in
ssh -i your-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

## Step 4: Clone Repository

```bash
# Clone your BuzzLink repository
git clone https://github.com/YOUR_USERNAME/BuzzLink.git
cd BuzzLink
```

## Step 5: Configure Environment Variables

1. **Copy the example environment file**:
```bash
cp .env.example .env
```

2. **Edit the .env file**:
```bash
nano .env
```

3. **Fill in your values**:

```env
# Database Configuration
POSTGRES_PASSWORD=your_secure_password_here

# Clerk Authentication
CLERK_PUBLIC_KEY=pk_live_xxxxx
CLERK_SECRET_KEY=sk_live_xxxxx
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_live_xxxxx

# Email Configuration (Gmail example)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your.email@gmail.com
SPRING_MAIL_PASSWORD=your_app_specific_password

# Application URLs (replace with YOUR EC2 public IP)
APP_BASE_URL=http://YOUR_EC2_PUBLIC_IP:3000
NEXT_PUBLIC_API_URL=http://YOUR_EC2_PUBLIC_IP:8080
NEXT_PUBLIC_WS_URL=ws://YOUR_EC2_PUBLIC_IP:8080
```

**How to get Clerk keys**:
- Go to https://clerk.com → Your app → API Keys
- Copy the Publishable Key and Secret Key
- **IMPORTANT**: Also configure Clerk allowed origins in Clerk Dashboard:
  - Add `http://YOUR_EC2_PUBLIC_IP:3000` to allowed origins

**How to get Gmail app password**:
- Enable 2-factor authentication on your Google account
- Go to Google Account → Security → 2-Step Verification → App passwords
- Generate a new app password for "Mail"

4. **Save and exit**: Press `Ctrl+X`, then `Y`, then `Enter`

## Step 6: Deploy Application

```bash
# Make deploy script executable
chmod +x scripts/deploy.sh

# Run deployment
./scripts/deploy.sh
```

The deployment process will:
1. Stop any existing containers
2. Build Docker images (takes 5-10 minutes)
3. Start all services (PostgreSQL, Backend, Frontend)
4. Show service status

## Step 7: Verify Deployment

1. **Check service status**:
```bash
docker-compose ps
```

All services should show "Up" status.

2. **View logs**:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

3. **Access your application**:
- **Frontend**: `http://YOUR_EC2_PUBLIC_IP:3000`
- **Backend API**: `http://YOUR_EC2_PUBLIC_IP:8080`

## Step 8: Configure Clerk Redirect URLs

In your Clerk Dashboard:
1. Go to **User & Authentication** → **Redirects**
2. Add redirect URLs:
   - Sign-in: `http://YOUR_EC2_PUBLIC_IP:3000`
   - Sign-up: `http://YOUR_EC2_PUBLIC_IP:3000`
   - After sign-in: `http://YOUR_EC2_PUBLIC_IP:3000/chat`
   - After sign-out: `http://YOUR_EC2_PUBLIC_IP:3000`

## Common Commands

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs -f

# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart backend

# Stop all services
./scripts/stop.sh
# OR
docker-compose down

# Redeploy (pull latest code and rebuild)
./scripts/deploy.sh

# Check resource usage
docker stats
```

## Troubleshooting

### Services not starting

```bash
# Check logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres

# Rebuild from scratch
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Backend can't connect to database

- Check PostgreSQL is running: `docker-compose ps postgres`
- Check database logs: `docker-compose logs postgres`
- Verify `POSTGRES_PASSWORD` in `.env` matches in backend connection string

### Frontend can't connect to backend

- Check backend is running: `docker-compose ps backend`
- Verify `NEXT_PUBLIC_API_URL` in `.env` uses correct EC2 public IP
- Verify `NEXT_PUBLIC_WS_URL` uses `ws://` (not `wss://`)

### Clerk authentication not working

- Verify all Clerk environment variables are set correctly
- Check allowed origins in Clerk Dashboard include `http://YOUR_EC2_PUBLIC_IP:3000`
- Check redirect URLs in Clerk Dashboard

### Email invitations not sending

- Verify Gmail app password (not regular password)
- Check 2-factor authentication is enabled on Google account
- Try using different SMTP provider if Gmail doesn't work

### Port already in use

```bash
# Check what's using the port
sudo lsof -i :8080
sudo lsof -i :3000

# Kill the process
sudo kill -9 <PID>
```

## Updating Your Application

When you make code changes:

```bash
# On your local machine, push to GitHub
git add .
git commit -m "Your changes"
git push origin main

# On EC2, pull and redeploy
cd ~/BuzzLink
git pull origin main
./scripts/deploy.sh
```

## Monitoring

```bash
# Monitor resource usage
docker stats

# Check disk space
df -h

# Check memory
free -h

# View system logs
docker-compose logs -f --tail=100
```

## Security Recommendations

1. **Change default PostgreSQL password**: Use a strong password in `.env`
2. **Restrict SSH access**: Only allow your IP in EC2 security group
3. **Use HTTPS**: For production, set up a domain name and SSL certificate (Let's Encrypt)
4. **Keep system updated**: Regularly run `sudo apt-get update && sudo apt-get upgrade`
5. **Backup database**: Periodically backup PostgreSQL data volume

## Stopping the Application

```bash
# Stop all services
./scripts/stop.sh

# Stop and remove all data (including database)
docker-compose down -v
```

## Free Tier Limits

AWS Free Tier includes:
- **EC2**: 750 hours/month of t2.micro (enough for 1 instance running 24/7)
- **Storage**: 30 GB of EBS storage
- **Data Transfer**: 15 GB outbound per month

**Note**: If you exceed these limits, you'll be charged. Monitor your usage in AWS Billing Dashboard.

## Cost After Free Tier (After 12 Months)

If you continue running after free tier expires:
- t2.micro EC2: ~$8-10/month
- 30 GB EBS: ~$3/month
- **Total**: ~$11-13/month

## Support

If you encounter issues:
1. Check logs: `docker-compose logs -f`
2. Verify environment variables in `.env`
3. Check EC2 security group rules
4. Verify Clerk configuration

---

**Congratulations!** Your BuzzLink application is now deployed on AWS EC2. 🎉
