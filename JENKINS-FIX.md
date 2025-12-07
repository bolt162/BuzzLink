# Jenkins Configuration Fix

## Give Jenkins User Docker Permissions

Since Jenkins is running on the same EC2 server, we need to give the `jenkins` user permission to run Docker commands.

**Run this on your EC2 server:**

```bash
# Add jenkins user to docker group
sudo usermod -aG docker jenkins

# Restart Jenkins to apply changes
sudo systemctl restart jenkins

# Verify jenkins user can run docker
sudo -u jenkins docker ps
```

## Push Updated Jenkinsfile

Now push the updated Jenkinsfile to GitHub:

```bash
git add Jenkinsfile
git commit -m "Fix: Run deployment locally instead of SSH"
git push origin main
```

Jenkins will automatically trigger and deploy!

## Changes Made:

✅ Removed SSH requirement - Jenkins runs commands locally
✅ Changed from `git reset --hard` to simple `git pull origin main`
✅ Simplified deployment process:
  1. Pull latest code
  2. Stop containers
  3. Build and start containers
  4. Health checks
  5. Cleanup old images

Much simpler and faster!
