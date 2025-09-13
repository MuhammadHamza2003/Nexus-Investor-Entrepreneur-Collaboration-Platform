// MongoDB initialization script for Nexus
// This script creates the application user and sets up the database

// Switch to nexus_prod database
db = db.getSiblingDB("nexus_prod");

// Create application user with read/write permissions
db.createUser({
  user: "nexus_user",
  pwd: "nexus_password",
  roles: [
    {
      role: "readWrite",
      db: "nexus_prod",
    },
  ],
});

// Create collections if they don't exist
db.createCollection("users");
db.createCollection("transactions");
db.createCollection("wallets");
db.createCollection("payment_methods");
db.createCollection("documents");
db.createCollection("meetings");

// Create indexes for better performance
// User indexes
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });

// Transaction indexes
db.transactions.createIndex({ userId: 1 });
db.transactions.createIndex({ status: 1 });
db.transactions.createIndex({ type: 1 });
db.transactions.createIndex({ createdAt: -1 });
db.transactions.createIndex({ externalId: 1 }, { unique: true, sparse: true });

// Wallet indexes
db.wallets.createIndex({ userId: 1 }, { unique: true });

// Payment method indexes
db.payment_methods.createIndex({ userId: 1 });
db.payment_methods.createIndex({ stripeId: 1 }, { unique: true, sparse: true });

// Document indexes
db.documents.createIndex({ uploadedBy: 1 });
db.documents.createIndex({ uploadDate: -1 });

// Meeting indexes
db.meetings.createIndex({ entrepreneurId: 1 });
db.meetings.createIndex({ investorId: 1 });
db.meetings.createIndex({ scheduledDate: 1 });
db.meetings.createIndex({ status: 1 });

print("Database initialization completed successfully!");
