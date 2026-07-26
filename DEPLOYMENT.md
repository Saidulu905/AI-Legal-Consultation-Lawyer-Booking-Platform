# Render Deployment Guide
# AI Legal Consultation & Lawyer Booking Platform

This guide will help you deploy the entire platform (frontend, backend, and database) on Render using the existing `render.yaml` blueprint.

## Prerequisites

1. **Render Account**: Create a free account at [render.com](https://render.com)
2. **GitHub Repository**: Your code must be pushed to a GitHub repository
3. **Gemini API Key**: Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

## Quick Deployment (Using Blueprint)

### Step 1: Push Code to GitHub

```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### Step 2: Deploy on Render

1. Log in to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"Blueprint"**
3. Connect your GitHub repository
4. Render will automatically detect the `render.yaml` file
5. Review the configuration:
   - **Database**: PostgreSQL (free tier)
   - **Backend**: Docker-based Spring Boot API
   - **Frontend**: Static React + Vite site
6. Click **"Apply"** to start deployment

### Step 3: Set GEMINI_API_KEY

After deployment:

1. Go to your **legal-platform-api** service in Render
2. Navigate to **Environment** tab
3. Add environment variable:
   - Key: `GEMINI_API_KEY`
   - Value: Your actual Gemini API key
4. Click **"Save Changes"**
5. **Restart** the service to apply changes

## Services Overview

### 1. PostgreSQL Database
- **Name**: legal-platform-db
- **Plan**: Free tier
- **Database**: legal_platform
- **User**: legal_platform_user
- **Connection**: Automatically linked to backend via `DATABASE_URL`

### 2. Backend API (Spring Boot)
- **Name**: legal-platform-api
- **Runtime**: Docker
- **Port**: 8080
- **Health Check**: `/health`
- **Environment Variables**:
  - `DATABASE_URL`: Auto-populated from database
  - `JWT_SECRET`: Auto-generated
  - `GEMINI_API_KEY`: Manual setup required

### 3. Frontend (React + Vite)
- **Name**: legal-platform-frontend
- **Runtime**: Static site
- **Build Command**: `npm install && npm run build`
- **Publish Directory**: `dist`
- **Environment Variables**:
  - `VITE_API_BASE_URL`: Auto-populated from backend URL

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
