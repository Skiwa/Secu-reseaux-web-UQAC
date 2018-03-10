﻿// <auto-generated />
using KeyRing.model;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;
using Microsoft.EntityFrameworkCore.Storage;
using System;

namespace KeyRing.Migrations
{
    [DbContext(typeof(KeyRingStore))]
    partial class KeyRingStoreModelSnapshot : ModelSnapshot
    {
        protected override void BuildModel(ModelBuilder modelBuilder)
        {
#pragma warning disable 612, 618
            modelBuilder
                .HasAnnotation("ProductVersion", "2.0.0-rtm-26452");

            modelBuilder.Entity("KeyRing.model.Password", b =>
                {
                    b.Property<string>("Username");

                    b.Property<string>("Tag");

                    b.Property<string>("StoredPassword");

                    b.HasKey("Username", "Tag");

                    b.ToTable("Passwords");
                });

            modelBuilder.Entity("KeyRing.model.User", b =>
                {
                    b.Property<string>("Username")
                        .ValueGeneratedOnAdd();

                    b.Property<string>("MasterPasswordHash");

                    b.Property<string>("Salt");

                    b.HasKey("Username");

                    b.ToTable("Users");
                });

            modelBuilder.Entity("KeyRing.model.Password", b =>
                {
                    b.HasOne("KeyRing.model.User", "User")
                        .WithMany("Passwords")
                        .HasForeignKey("Username")
                        .OnDelete(DeleteBehavior.Cascade);
                });
#pragma warning restore 612, 618
        }
    }
}
