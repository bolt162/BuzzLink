#!/bin/bash

# Deployment script for BuzzLink on EC2
# Run this from the project root directory

set -e

echo "========================================="
echo "BuzzLink Deployment Script"
echo "========================================="
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Please create a .env file from .env.example and fill in your values."
    echo "Run: cp .env.example .env"
    echo "Then edit .env with your actual values."
    exit 1
fi

# Load environment variables
source .env

# Check required environment variables
REQUIRED_VARS=("CLERK_SECRET_KEY" "NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY" "POSTGRES_PASSWORD")
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "ERROR: Required environment variable $var is not set in .env file"
        exit 1
    fi
done

echo "Step 1: Stopping existing containers..."
docker-compose down

echo ""
echo "Step 2: Pulling latest code..."
git pull origin main || echo "Skipping git pull (not in git repository or no remote)"

echo ""
echo "Step 3: Building Docker images..."
docker-compose build --no-cache

echo ""
echo "Step 4: Starting services..."
docker-compose up -d

echo ""
echo "Step 5: Waiting for services to be healthy..."
sleep 10

# Check service health
echo ""
echo "Checking service status..."
docker-compose ps

echo ""
echo "========================================="
echo "Deployment Complete!"
echo "========================================="
echo ""
echo "Your application should be running at:"
echo "  Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000"
echo "  Backend:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo ""
echo "To view logs:"
echo "  All services: docker-compose logs -f"
echo "  Frontend:     docker-compose logs -f frontend"
echo "  Backend:      docker-compose logs -f backend"
echo "  Database:     docker-compose logs -f postgres"
echo ""
echo "To stop services: docker-compose down"
echo "To restart:       docker-compose restart"
