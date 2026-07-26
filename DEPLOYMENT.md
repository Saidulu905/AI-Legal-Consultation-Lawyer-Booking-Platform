# Render Deployment Guide
# AI Legal Consultation & Lawyer Booking Platform

This guide will help you deploy the entire platform (frontend, backend, and database) on Render using manual service creation.

## Prerequisites

1. **Render Account**: Create a free account at [render.com](https://render.com)
2. **GitHub Repository**: Your code must be pushed to a GitHub repository
3. **Gemini API Key**: Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

## Manual Deployment Steps

### Step 1: Create PostgreSQL Database

1. Log in to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"PostgreSQL"**
3. Configure database:
   - **Name**: `legal-platform-db`
   - **Database**: `legal_platform`
   - **User**: `legal_platform_user`
   - **Region**: Choose nearest region
   - **Plan**: Free
4. Click **"Create Database"**
5. Wait for database to be "Available" (may take 2-3 minutes)
6. Copy the **Internal Database URL** from the database page

### Step 2: Deploy Backend (Spring Boot API)

1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repository
3. Configure service:
   - **Name**: `legal-platform-api`
   - **Region**: Same as database
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: Docker
   - **Plan**: Free
4. Add Environment Variables:
   - `DATABASE_URL`: Paste the Internal Database URL from Step 1
   - `JWT_SECRET`: Generate a strong random string (use: `openssl rand -base64 32`)
   - `GEMINI_API_KEY`: Your actual Gemini API key
5. Click **"Create Web Service"**
6. Wait for deployment to complete

### Step 3: Deploy Frontend (React + Vite)

1. Click **"New +"** → **"Static Site"**
2. Connect your GitHub repository
3. Configure service:
   - **Name**: `legal-platform-frontend`
   - **Region**: Same as backend
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
   - **Plan**: Free
4. Add Environment Variables:
   - `VITE_API_BASE_URL`: Use the backend URL (e.g., `https://legal-platform-api.onrender.com`)
5. Click **"Create Static Site"**
6. Wait for deployment to complete

## Services Overview

### 1. PostgreSQL Database
- **Name**: legal-platform-db
- **Plan**: Free tier
- **Database**: legal_platform
- **User**: legal_platform_user
- **Connection**: Manually linked via `DATABASE_URL` environment variable

### 2. Backend API (Spring Boot)
- **Name**: legal-platform-api
- **Runtime**: Docker
- **Port**: 8080
- **Health Check**: `/health`
- **Environment Variables**:
  - `DATABASE_URL`: Manually set from database Internal Database URL
  - `JWT_SECRET`: Manually generated
  - `GEMINI_API_KEY`: Manually set

### 3. Frontend (React + Vite)
- **Name**: legal-platform-frontend
- **Runtime**: Static site
- **Build Command**: `npm install && npm run build`
- **Publish Directory**: `dist`
- **Environment Variables**:
  - `VITE_API_BASE_URL`: Manually set to backend URL

## Verification

After deployment completes:

1. **Check Backend Health**:
   ```
   curl https://legal-platform-api.onrender.com/health
   ```
   Expected response: `{"status":"UP"}`

2. **Access Frontend**:
   ```
   https://legal-platform-frontend.onrender.com
   ```

3. **Check Render Dashboard**:
   - All services should show "Live" status
   - No errors in the logs

## Troubleshooting

### Backend Fails to Start
- Check logs in Render Dashboard
- Verify DATABASE_URL is properly set
- Ensure GEMINI_API_KEY is configured

### Frontend Build Fails
- Check `package.json` scripts
- Verify build command: `npm install && npm run build`
- Check for Node.js version compatibility

### Database Connection Issues
- Verify database is in "Available" state
- Check DATABASE_URL format
- Review backend logs for connection errors

### API Calls from Frontend Fail
- Verify VITE_API_BASE_URL is set correctly
- Check CORS configuration in backend
- Ensure backend is running and accessible

## Local Development vs Production

### Local Development
- Uses `.env` file for configuration
- Database: Local PostgreSQL
- API URL: `http://localhost:8080`

### Production (Render)
- Uses Render environment variables
- Database: Render PostgreSQL
- API URL: Auto-generated Render URL

## Cost Summary (Free Tier)

- **PostgreSQL**: Free (90 days, then $7/month)
- **Backend (Docker)**: Free (750 hours/month)
- **Frontend (Static)**: Free
- **Total**: $0/month (after free database period: $7/month)

## Scaling

To upgrade from free tier:

1. Go to service settings in Render
2. Change plan from "Free" to "Starter" or higher
3. Adjust resources as needed

## Support

For issues specific to:
- **Render**: [Render Documentation](https://render.com/docs)
- **Platform**: Check GitHub issues or contact development team

## Security Notes

- Never commit `.env` file to Git
- Use strong JWT_SECRET in production
- Keep GEMINI_API_KEY secure
- Enable SSL (automatic on Render)
- Regular database backups (automatic on Render paid tiers)
