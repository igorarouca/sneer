package sneer.bricks.hardware.cpu.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;


import basis.brickness.Brick;


@Brick
public interface Crypto {

	Hash digest(byte[] input);
	Hash digest(File file) throws IOException;	
	Hash digest(Path file) throws IOException;

	Digester newDigester();

	Hash unmarshallHash(byte[] bytes);
	
	Signature getSHA512WithECDSA();
	KeyPair newECDSAKeyPair(byte[] seed);
	
	ECBCipher newAES256Cipher(byte[] encryptKey, byte[] decryptKey);
	
	PublicKey unmarshalPublicKey(byte[] keyBytes);
	Hash secretKeyFrom(PublicKey publicKey, PrivateKey privateKey, byte[] sessionKey);

}
