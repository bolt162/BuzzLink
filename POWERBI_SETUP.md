# Power BI Dashboard Setup Guide

This guide walks you through setting up a Power BI dashboard for BuzzLink analytics.

## Prerequisites

1. **Power BI Desktop** installed on your machine ([Download here](https://powerbi.microsoft.com/desktop/))
2. **PostgreSQL ODBC Driver** installed ([Download here](https://www.postgresql.org/ftp/odbc/versions/msi/))
3. **BuzzLink backend running** with PostgreSQL database
4. **Mock data loaded** into the database

## Step 1: Load Mock Data into Database

### Option A: Via SQL Script (Recommended)

1. SSH into your EC2 instance:
```bash
ssh -i ~/.ssh/BuzzLinkKey.pem ubuntu@184.169.147.113
```

2. Navigate to the project and load the mock data:
```bash
cd /home/ubuntu/BuzzLink/backend/src/main/resources
docker exec -i $(docker ps -qf "name=postgres") psql -U postgres -d buzzlink < data-mock.sql
```

### Option B: Via Docker Exec

```bash
# Copy SQL file to container
docker cp data-mock.sql buzzlink-db:/tmp/

# Execute SQL
docker exec buzzlink-db psql -U postgres -d buzzlink -f /tmp/data-mock.sql
```

### Verification

```bash
docker exec buzzlink-db psql -U postgres -d buzzlink -c "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM messages;"
```

You should see:
- 20 users
- 550+ messages
- 5 workspaces
- 14 channels

## Step 2: Configure PostgreSQL for Remote Access

### Allow External Connections (if needed)

1. Edit `postgresql.conf`:
```bash
docker exec -it buzzlink-db bash
echo "listen_addresses = '*'" >> /var/lib/postgresql/data/postgresql.conf
```

2. Edit `pg_hba.conf`:
```bash
echo "host all all 0.0.0.0/0 md5" >> /var/lib/postgresql/data/pg_hba.conf
```

3. Restart PostgreSQL container:
```bash
docker-compose restart postgres
```

### Security Note

For production, use more restrictive rules. The above allows connections from any IP for demo purposes.

## Step 3: Install PostgreSQL ODBC Driver

### Windows

1. Download [PostgreSQL ODBC Driver](https://www.postgresql.org/ftp/odbc/versions/msi/)
2. Install `psqlodbc_x64.msi` (64-bit version for Power BI Desktop)
3. Verify installation via **ODBC Data Sources (64-bit)** in Windows

### macOS

```bash
brew install psqlodbc
```

### Linux

```bash
sudo apt-get install odbc-postgresql
```

## Step 4: Connect Power BI to PostgreSQL

### Open Power BI Desktop

1. Launch Power BI Desktop
2. Click **Get Data** → **More...**
3. Search for **PostgreSQL database**
4. Click **Connect**

### Connection Details

Enter the following:
- **Server**: `184.169.147.113` (your EC2 IP) or `localhost` if running locally
- **Database**: `buzzlink`
- **Data Connectivity mode**: **Import** (recommended for better performance)

Click **OK**

### Authentication

1. Select **Database** tab
2. Enter:
   - **User name**: `postgres`
   - **Password**: `MrRobot2024!` (from your `.env` file)
3. Click **Connect**

### Load Tables

You'll see all BuzzLink tables:
- ✅ users
- ✅ workspaces
- ✅ channels
- ✅ messages
- ✅ direct_messages
- ✅ reactions
- ✅ notifications
- ✅ user_workspace_members

**Select all tables** and click **Transform Data** to open Power Query Editor

## Step 5: Create Relationships in Power BI

Power BI may auto-detect relationships, but verify these exist:

1. Go to **Model** view (left sidebar)
2. Create/verify relationships:

### Key Relationships

- `messages.sender_id` → `users.id` (Many-to-One)
- `messages.channel_id` → `channels.id` (Many-to-One)
- `channels.workspace_id` → `workspaces.id` (Many-to-One)
- `user_workspace_members.user_id` → `users.id` (Many-to-One)
- `user_workspace_members.workspace_id` → `workspaces.id` (Many-to-One)
- `reactions.message_id` → `messages.id` (Many-to-One)
- `reactions.user_id` → `users.id` (Many-to-One)
- `direct_messages.sender_id` → `users.id` (Many-to-One)
- `direct_messages.recipient_id` → `users.id` (Many-to-One)

## Step 6: Create Calculated Measures

Go to **Data** view and create these DAX measures:

### Total Metrics

```dax
Total Messages = COUNT(messages[id])

Total Users = COUNT(users[id])

Total Workspaces = COUNT(workspaces[id])

Total Channels = COUNT(channels[id])

Total Reactions = COUNT(reactions[id])

Active Users =
CALCULATE(
    DISTINCTCOUNT(messages[sender_id]),
    messages[created_at] >= TODAY() - 30
)

Messages This Week =
CALCULATE(
    COUNT(messages[id]),
    messages[created_at] >= TODAY() - 7
)

Messages This Month =
CALCULATE(
    COUNT(messages[id]),
    messages[created_at] >= TODAY() - 30
)

Avg Messages Per User =
DIVIDE([Total Messages], [Total Users], 0)

Avg Messages Per Channel =
DIVIDE([Total Messages], [Total Channels], 0)

Message Growth Rate =
VAR CurrentMonth = [Messages This Month]
VAR PreviousMonth =
CALCULATE(
    COUNT(messages[id]),
    messages[created_at] >= TODAY() - 60 && messages[created_at] < TODAY() - 30
)
RETURN
DIVIDE(CurrentMonth - PreviousMonth, PreviousMonth, 0) * 100
```

## Step 7: Create Dashboard Visualizations

Create a new dashboard with these visuals:

### Page 1: Overview Dashboard

1. **KPI Cards** (Top row):
   - Total Users (card visual)
   - Total Messages (card visual)
   - Active Users (30 days) (card visual)
   - Message Growth Rate % (card visual)

2. **Messages Over Time** (Line chart):
   - X-axis: `messages[created_at]` (by day)
   - Y-axis: `Total Messages`
   - Show trend line

3. **Messages by Workspace** (Bar chart):
   - X-axis: `workspaces[name]`
   - Y-axis: `Total Messages`
   - Sort descending

4. **Top Active Users** (Table):
   - Columns: `users[display_name]`, `Total Messages`
   - Sort by Total Messages descending
   - Top N: 10

5. **Messages by Channel** (Pie chart):
   - Legend: `channels[name]`
   - Values: `Total Messages`

6. **Activity Heatmap** (Matrix):
   - Rows: Day of week
   - Columns: Hour of day
   - Values: `Total Messages`

### Page 2: User Analytics

1. **User Growth Trend** (Line chart):
   - X-axis: `users[created_at]` (by week)
   - Y-axis: Cumulative user count

2. **User Activity Distribution** (Histogram):
   - X-axis: Message count buckets
   - Y-axis: Number of users

3. **Admin vs Regular Users** (Pie chart):
   - Legend: `users[is_admin]`
   - Values: `Total Users`

4. **Top Workspaces by Members** (Bar chart):
   - X-axis: `workspaces[name]`
   - Y-axis: Count of `user_workspace_members`

### Page 3: Engagement Metrics

1. **Reaction Distribution** (Donut chart):
   - Legend: `reactions[emoji]`
   - Values: `Total Reactions`

2. **Messages by Type** (Pie chart):
   - Legend: `messages[type]`
   - Values: Count

3. **Most Active Channels** (Table):
   - Columns: Channel name, Total messages, Total reactions
   - Sort by messages

4. **Direct Messages Trend** (Line chart):
   - X-axis: `direct_messages[created_at]` (by day)
   - Y-axis: Count of DMs

## Step 8: Apply Themes and Formatting

1. Go to **View** → **Themes**
2. Select a professional theme (e.g., "Executive")
3. Customize colors:
   - Primary: `#3B82F6` (blue)
   - Secondary: `#10B981` (green)
   - Accent: `#F59E0B` (amber)

4. Add filters:
   - Date range slicer
   - Workspace filter
   - Channel filter

## Step 9: Publish Dashboard (Optional)

### To Power BI Service

1. Click **Publish** in Power BI Desktop
2. Sign in to Power BI Service
3. Select workspace
4. Share with stakeholders

### Export as PDF

1. Go to **File** → **Export** → **Export to PDF**
2. Select pages to include
3. Save PDF report

## Troubleshooting

### Cannot Connect to Database

**Issue**: Connection timeout or refused

**Solutions**:
- Verify PostgreSQL is running: `docker ps | grep postgres`
- Check firewall rules allow port 5432
- Verify EC2 security group allows inbound PostgreSQL (5432)
- Test connection: `psql -h 184.169.147.113 -U postgres -d buzzlink`

### ODBC Driver Not Found

**Issue**: Power BI can't find PostgreSQL driver

**Solutions**:
- Reinstall 64-bit ODBC driver
- Restart Power BI Desktop after installation
- Check **ODBC Data Sources (64-bit)** has PostgreSQL entry

### Relationships Not Detected

**Issue**: Tables aren't connecting automatically

**Solutions**:
- Manually create relationships in Model view
- Ensure foreign key columns have matching data types
- Check for NULL values in join columns

### Slow Performance

**Issue**: Dashboard takes long to load

**Solutions**:
- Use **Import** mode instead of **DirectQuery**
- Create aggregated tables for large datasets
- Limit date range with filters
- Use incremental refresh settings

## Next Steps

1. **Scheduled Refresh**: Set up automated data refresh in Power BI Service
2. **Row-Level Security**: Implement workspace-based RLS for multi-tenant access
3. **Real-time Data**: Use DirectQuery mode for live dashboards
4. **Mobile Layout**: Create mobile-optimized version
5. **Alerts**: Set up data alerts for key metrics

## Sample Queries for Custom Metrics

### Most Active Hours
```sql
SELECT
    EXTRACT(HOUR FROM created_at) as hour,
    COUNT(*) as message_count
FROM messages
GROUP BY hour
ORDER BY message_count DESC;
```

### Workspace Engagement Score
```sql
SELECT
    w.name,
    COUNT(DISTINCT m.sender_id) as active_users,
    COUNT(m.id) as total_messages,
    COUNT(r.id) as total_reactions
FROM workspaces w
JOIN channels c ON c.workspace_id = w.id
JOIN messages m ON m.channel_id = c.id
LEFT JOIN reactions r ON r.message_id = m.id
GROUP BY w.id, w.name
ORDER BY total_messages DESC;
```

## Resources

- [Power BI Documentation](https://docs.microsoft.com/power-bi/)
- [DAX Function Reference](https://dax.guide/)
- [PostgreSQL ODBC Configuration](https://odbc.postgresql.org/)
- [Power BI Best Practices](https://docs.microsoft.com/power-bi/guidance/)
