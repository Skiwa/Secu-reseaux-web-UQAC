using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using Microsoft.AspNetCore.Cryptography.KeyDerivation;
using System.IO;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace KeyRing.model
{
    class Password
    {
        //Clé primaire constituée de 2 champs
        public string Username { get; set; }    //Foreign key vers la clé primaire de User
        public string Tag { get; set; }         //Et le tag associé au mot de passe

        //Mot de passe chiffré en AES-256, encodé en base 64
        public string StoredPassword { get; set; }

        //Concrétise la foreign key vers l'entité User
        [ForeignKey("Username")]
        public User User { get; set; }

        public User LoadUser(KeyRingStore db)
        {
            //Charger la référence vers l'utilisateur
            db.Entry(this).Reference(p => p.User).Load();

            //Retourner l'utilisateur chargé
            return User;
        }


        /**
         * Retourne une clé de dérivation générée à partir du master password et du sel cryptogrpahique
         */
        private byte[] GetDerivationKey(string MasterPassword, string userSalt)
        {
            //Obtenir le tableau d'octets du salt
            byte[] saltBytes = Convert.FromBase64String(userSalt);

            //128 bits = 16 bytes
            return KeyDerivation.Pbkdf2(MasterPassword, saltBytes, KeyDerivationPrf.HMACSHA256, 10000, 16);
        }

        private byte[] GetEncryptedPassword(string password, byte[] derivationKey)
        {
            using (Aes aes = new AesManaged())
            {
                //Définir la clé de dérivation et l'IV
                aes.Key = derivationKey;
                aes.GenerateIV(); //Générer un vecteur d'initialisation aléatoire

                //Créer un encrypteur
                var encryptor = aes.CreateEncryptor();

                //Créer le stream de chiffrement
                using (var memStream = new MemoryStream())
                {
                    using (var encryptionStream = new CryptoStream(memStream, encryptor, CryptoStreamMode.Write))
                    {
                        using (var streamWriter = new StreamWriter(encryptionStream))
                        {
                            //Écrire le mot de passe dans le stream d'encryption
                            streamWriter.Write(password);
                        }

                        //Récupérer la version chiffrée dans un tableau d'octets
                        byte[] cypher = memStream.ToArray();

                        //Concatener l'IV et le cipher
                        byte[] IVandCipher = new byte[aes.IV.Length + cypher.Length];
                        Array.Copy(aes.IV, 0, IVandCipher, 0, aes.IV.Length);    //Commencer par l'IV
                        Array.Copy(cypher, 0, IVandCipher, aes.IV.Length, cypher.Length); //Puis le cipher

                        return IVandCipher;
                    }
                }
            }
        }

        private string GetDecryptedPassword(byte[] IVandCipher, byte[] derivationKey)
        {
            using (Aes aes = new AesManaged())
            {
                //Générer un IV bidon juste pour obtenir la taille d'un IV
                aes.GenerateIV();

                //Alimenter les variables nécessaire pour séparer l'IV du cypher
                int ivLength = aes.IV.Length;
                byte[] iv = new byte[ivLength];
                int cipherLength = IVandCipher.Length - ivLength;

                if (cipherLength < 0)
                {
                    throw new Exception("Taille du cipher invalide");
                }
                byte[] cipher = new byte[cipherLength];

                //Séparer le IV et le cipher
                Array.Copy(IVandCipher, 0, iv, 0, ivLength); //Récupérer la partie IV
                Array.Copy(IVandCipher, ivLength, cipher, 0, cipherLength); //Récupérer le partie cipher

                //Définir la clé de dérivation et l'IV
                aes.Key = derivationKey;
                aes.IV = iv;

                //Créer un décrypteur
                var decryptor = aes.CreateDecryptor();

                //Créer le stream de chiffrement
                using (var memStream = new MemoryStream(cipher))
                {
                    using (var decryptionStream = new CryptoStream(memStream, decryptor, CryptoStreamMode.Read))
                    {
                        //Initialiser le tableau d'octet pour recevoir le mot de passe déchiffré
                        //NOTE: On ne connait pas sa taille, mais ce sera au maximum la taille du cipher
                        byte[] decrypted = new byte[cipherLength];

                        //Lire le mot de passe déchiffré dans le tableau d'octets
                        decryptionStream.Read(decrypted, 0, cipherLength);

                        //Réassembler le mot de passe sous forme de string
                        string clearPassword = Encoding.UTF8.GetString(decrypted);

                        //Retirer les "\0" en trop à la fin
                        while (clearPassword.EndsWith('\0'))
                        {
                            clearPassword = clearPassword.Substring(0, clearPassword.Length - 1);
                        }

                        return clearPassword;
                    }
                }
            }
        }

        public void EncryptPassword(string clearPassword, string MasterPassword, string userSalt)
        {
            byte[] derivationKey = GetDerivationKey(MasterPassword, userSalt);

            byte[] encrypted = GetEncryptedPassword(clearPassword, derivationKey);

            StoredPassword = Convert.ToBase64String(encrypted);
        }

        public string DecryptPassword(string MasterPassword, string userSalt)
        {
            //Obtenir la version "tableau d'octets" depuis la version Base64 de la concaténation de l'IV et du mot de passe chiffré
            byte[] IVandEncryptedPwd = Convert.FromBase64String(StoredPassword);

            //Reconstruire la clé de dérivation à partir du Master Password et du sel de l'usager
            byte[] derivationKey = GetDerivationKey(MasterPassword, userSalt);

            //Décrypter le mot de passe
            return GetDecryptedPassword(IVandEncryptedPwd, derivationKey);
        }

        
    }

}
