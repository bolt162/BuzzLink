# 🚀 BuzzLink - Quick Reference Card

## One-Line Deployment

```bash
./scripts/setup-terraform.sh && ./scripts/terraform-deploy.sh
```

## Essential Commands

### Deploy to AWS
```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### Get Deployment Info
```bash
cd terraform
terraform output
```

### SSH into Instance
```bash
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP
```

### Check App Status
```bash
# After SSH
docker ps
docker-compose ps
docker-compose logs -f
```

### Update Application
```bash
# After SSH
cd /home/ubuntu/BuzzLink
git pull
docker-compose down
docker-compose up -d --build
```

### Set Admin User
```bash
# After SSH
docker exec -it buzzlink-postgres psql -U buzzlink_user -d buzzlink
UPDATE users SET is_admin = true WHERE clerk_id = 'YOUR_CLERK_ID';
\q
```

### Destroy Everything
```bash
cd terraform
terraform destroy
```

## URLs After Deployment

- **Frontend:** `http://YOUR_IP:3000`
- **Backend:** `http://YOUR_IP:8080`
- **Admin:** `http://YOUR_IP:3000/admin`

## Required Information

- AWS Access Key ID
- AWS Secret Access Key
- Your public IP (from whatismyip.com)
- GitHub repo URL
- Clerk publishable key
- PostgreSQL password

## Files to Edit

Only one file needs your input:

**terraform/terraform.tfvars** (created by setup script)

## Documentation Files

| File | Purpose |
|------|---------|
| [WHAT_YOU_NEED_TO_DO.md](WHAT_YOU_NEED_TO_DO.md) | **START HERE** - Quick 5-step guide |
| [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) | Complete checklist with checkboxes |
| [INFRASTRUCTURE_SETUP.md](INFRASTRUCTURE_SETUP.md) | Detailed setup guide |
| [terraform/README.md](terraform/README.md) | Terraform documentation |

## Troubleshooting

### App not loading?
```bash
ssh -i ~/.ssh/id_rsa ubuntu@YOUR_IP
sudo cat /var/log/user-data.log
docker-compose logs
```

### Can't SSH?
Update your IP in `terraform.tfvars` and run `terraform apply`

### Forgot your instance IP?
```bash
cd terraform
terraform output instance_public_ip
```

## Cost

- **Free Tier (12 months):** $0/month
- **After Free Tier:** ~$10/month
- **Stop charges:** Run `terraform destroy`

## Support

Check the documentation files above for detailed help!
