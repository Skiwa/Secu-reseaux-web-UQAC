using Microsoft.EntityFrameworkCore;

using System;
using System.Collections.Generic;
using System.Text;

namespace KeyRing.model
{
    class KeyRingStore : DbContext
    {
        public DbSet<User> Users { get; set; }
        public DbSet<Password> Passwords { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.UseSqlite("Data Source=KeyRing.db");
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            //Définir la clé primaire composée de l'entité Password
            modelBuilder.Entity<Password>()
                .HasKey(c => new { c.Username, c.Tag });
        }
    }
}
