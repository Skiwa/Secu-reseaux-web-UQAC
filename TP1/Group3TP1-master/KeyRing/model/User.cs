using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.ComponentModel.DataAnnotations;

namespace KeyRing.model
{
    /**
     * Classe du modèle de données stockant les informations sur un utilisateur du "KeyRing"
     */
    class User
    {
        //Nom d'utilisateur tel que passé en paramètre aux différentes fonctions de l'application
        [Key]
        public string Username { get; set; }
        //Sel injecté dans le hash SHA256 du mot de passe
        public string Salt { get; set; }
        //Version hashée (SHA256) et encodée en base64 du Master Password
        public string MasterPasswordHash { get; set; }


        //Collection des mots de passes stockés de l'utilisateur (pour lien 1-n avec Password)
        public ICollection<Password> Passwords { get; set; }


        /**
         * Alimente un sel générée de façon aléatoire
         */
        private void SetRandomSalt()
        {
            //Créer un sel de 16 octets
            byte[] SaltBytes = new byte[16];

            using (RandomNumberGenerator rng = new RNGCryptoServiceProvider())
            {
                rng.GetBytes(SaltBytes);
            }

            //Convertir en base 64
            Salt = Convert.ToBase64String(SaltBytes);
        }

        /**
         * Retourne un hash SHA-256 bits du mot de passe fourni en paramètre
         */
        private byte[] GetSHA(string password)
        {
            //Obtenir les 16 octets du salt
            byte[] saltBytes = Convert.FromBase64String(Salt);

            //Concatenate password and salt
            byte[] pwdBytes = Encoding.UTF8.GetBytes(password);
            byte[] toHash = new byte[pwdBytes.Length + Salt.Length];
            Array.Copy(pwdBytes, 0, toHash, 0, pwdBytes.Length);
            Array.Copy(saltBytes, 0, toHash, pwdBytes.Length, saltBytes.Length);

            using (SHA256 sha = new SHA256Managed())
            {
                byte[] hash = sha.ComputeHash(toHash);
                return hash;
            }
        }

        /**
         * Retourne un encodage en Base 64 du hash du mot de passe.
         */
        private String GetBase64SHA(string password)
        {
            return Convert.ToBase64String(GetSHA(password));
        }

        /**
         * Définit le mot Master Password de l'usager, mais ne conserve que son hash SHA-256 encodé en base 64
         */
        public void SetMasterPassword(string MasterPassword)
        {
            //Initialise un sel aléatoire
            SetRandomSalt();
            //Obtient une version hashée et encodée du master password, et le conserve pour stockage
            this.MasterPasswordHash = GetBase64SHA(MasterPassword);
        }

        /**
         * Vérifie si le master password est valide
         */
        public bool TestMasterPassword(string MasterPassword) {
            string pwdTest = GetBase64SHA(MasterPassword);

            return pwdTest.Equals(this.MasterPasswordHash);
        }

        /**
         * S'assure que le master password est valide, lève une exception en cas d'erreur
         */
        public void AssertMasterPassword(string MasterPassword)
        {
            if (!TestMasterPassword(MasterPassword))
            {
                throw new Exception("Mauvais mot de passe");
            }
        }

    }
}
