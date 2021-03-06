/*
Copyright 2012, Jernej Kovacic

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 

package com.jkovacic.cryptoutil;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.math.*;

/**
 * A convenience asymmetric key pair handling class. It can be used to 
 * generate keys from their parameters, store keypairs etc.
 * 
 * Currently, RSA, DSA and ECDSA are supported.
 * 
 * @author Jernej Kovacic
 */
public class KeyCreator 
{
	// private and public key
	private PrivateKey prvkey = null;
	private PublicKey pubkey = null;
	
	// key type (encryption algorithm)
	private AsymmetricAlgorithm pktype = null;
	
	/*
	 * An empty constructor. It is private and used by instantiation factories
	 */
	private KeyCreator()
	{
	}
	
	/**
	 * Create a RSA key pair by passing key parameters
	 * 
	 * @param n - modulus
	 * @param e - public exponent
	 * @param d - private exponent
	 * 
	 * @return an instance of a RSA key or null if invalid parameters
	 */
	public static KeyCreator createRSAinstance(byte[] n, byte[] e, byte[] d)
	{
		// sanity check
		if ( null==n || null==e || null == d ||
			 0==n.length || 0==e.length || 0==d.length	)
		{
			return null;
		}
		
		KeyFactory factory = null;
		try
		{
			// instantiate a RSA factory, ...
			factory = KeyFactory.getInstance(AsymmetricAlgorithm.RSA.getName());	
		}
		catch ( NoSuchAlgorithmException ex )
		{
			return null;
		}
		
		// ...convert parameters to BigIntegers, ...
		BigInteger eint = new BigInteger(e);   // public exponent
		BigInteger nint = new BigInteger(n);   // modulus
		BigInteger dint = new BigInteger(d);   // private exponent
		
		// ...assign keys' specifications, ...
		KeySpec privspec = new RSAPrivateKeySpec(nint, dint);
		KeySpec pubspec = new RSAPublicKeySpec(nint, eint);
		
		PrivateKey prvk = null;
		PublicKey pubk = null;
		
		try
		{
			// ...and finally generate the keys
			prvk = factory.generatePrivate(privspec);
			pubk = factory.generatePublic(pubspec);
		}
		catch ( InvalidKeySpecException ex )
		{
			return null;
		}
		
		if ( null==prvk || null==pubk )
		{
			return null;
		}
		
		// key creation successful, assign the class's properties
		KeyCreator kc = new KeyCreator();
		kc.pktype = AsymmetricAlgorithm.RSA;
		kc.prvkey = prvk;
		kc.pubkey = pubk;
		
		return kc;
	}
	
	/**
	 * Create a DSA key pair by passing key parameters
	 * 
	 * @param p - prime
	 * @param q - sub prime
	 * @param g - base
	 * @param y - public key
	 * @param x - private key
	 * 
	 * @return an instance of a DSA key or null if invalid parameters
	 */
	public static KeyCreator createDSAinstance(byte[] p, byte[] q, byte[] g, byte[] y, byte[] x)
	{
		// sanity check
		if ( null==p || null==q || null==g || null==y || null==x ||
			 0==p.length || 0==q.length || 0==g.length || 0==y.length || 0==x.length )
		{
			return null;
		}
		
		KeyFactory factory = null;
		try
		{
			// instantiate a DSA factory, ...
			factory = KeyFactory.getInstance(AsymmetricAlgorithm.DSA.getName());	
		}
		catch ( NoSuchAlgorithmException ex )
		{
			return null;
		}
		
		// ...convert parameters to BigIntegers, ...
		BigInteger pint = new BigInteger(p);	// prime
		BigInteger qint = new BigInteger(q);	// sub prime
		BigInteger gint = new BigInteger(g);	// base
		BigInteger yint = new BigInteger(y);	// public key
		BigInteger xint = new BigInteger(x);	// private key 
		
		// ...assign keys' specifications, ...
		KeySpec privspec = new DSAPrivateKeySpec(xint, pint, qint, gint);
		KeySpec pubspec = new DSAPublicKeySpec(yint, pint, qint, gint);
		
		PrivateKey prvk = null;
		PublicKey pubk = null;
		
		try
		{
			// ...and finally generate the keys
			prvk = factory.generatePrivate(privspec);
			pubk = factory.generatePublic(pubspec);
		}
		catch ( InvalidKeySpecException ex )
		{
			return null;
		}
		
		if ( null==prvk || null==pubk )
		{
			return null;
		}
		
		// key creation successful, assign the class's properties
		KeyCreator kc = new KeyCreator();
		kc.pktype = AsymmetricAlgorithm.DSA;
		kc.prvkey = prvk;
		kc.pubkey = pubk;
		
		return kc;
	}
		
	
	/**
	 * Create an EC key pair by passing key parameters
	 * 
	 * @param alg - key type (specifying the EC domain parameters)
	 * @param q - public key (as a bit string)
	 * @param d - private key (as an octet string)
	 * 
	 * @return an instance of an EC key or null if invalid parameters
	 */
	public static KeyCreator createECinstance(AsymmetricAlgorithm alg, byte[] q, byte[] d)
	{
		// sanity check
		if ( null==alg || null==q || null==d ||
				0==q.length || 0==d.length )
		{
			return null;
		}
		
		switch (alg)
		{
		case ECDSA_NISTP256:
		case ECDSA_NISTP384:
		case ECDSA_NISTP521:
			// algorithm type is correct, nothing actually to do inside this switch
			break;
			
		default:
			// unsupported algorithm or EC domain
			return null;
		}
		
		KeyFactory factory = null;
		try
		{
			// instantiate an EC factory, ...
			factory = KeyFactory.getInstance("EC");
		}
		catch ( NoSuchAlgorithmException ex )
		{
			return null;
		}
		
		// ...convert parameters to appropriate classes, ...
		BigInteger sint = EcUtil.octetStringToInteger(d);  // private key
		ECPoint  wpoint = EcUtil.octetStringToEcPoint(alg, q);  // public key
		ECParameterSpec spec = EcUtil.getSpec(alg);		// EC specifications
		
		if ( null==sint || null==wpoint || null== spec )
		{
			return null;
		}
		
		// validate the public key:
		if ( false == EcUtil.validPublicKey(alg, wpoint) )
		{
			return null;
		}
		
		// ...assign keys' specifications, ...
		KeySpec privspec = new ECPrivateKeySpec(sint, spec);
		KeySpec pubspec = new ECPublicKeySpec(wpoint, spec);
		
		PrivateKey prvk = null;
		PublicKey pubk = null;

		try
		{
			// ...and finally generate the keys
			prvk = factory.generatePrivate(privspec);
			pubk = factory.generatePublic(pubspec);
		}
		catch ( InvalidKeySpecException ex )
		{
			return null;
		}

		if ( null==prvk || null==pubk )
		{
			return null;
		}

		// key creation successful, assign the class's properties
		KeyCreator kc = new KeyCreator();
		kc.pktype = alg;
		kc.prvkey = prvk;
		kc.pubkey = pubk;

		return kc;
	}
	
	/**
	 * Creates a key pair (any of supported public asymmetric algorithms) by
	 * passing a public and its corresponding private key. 
	 * 
	 * @param pubKey - public key
	 * @param privKey - private key
	 * 
	 * @return an instance of a key or null if invalid or incompatible keys
	 */
	public static KeyCreator createFromKeys(PublicKey pubKey, PrivateKey privKey)
	{
		// sanity check
		if ( null==pubKey || null==privKey )
		{
			return null;
		}
		
		// Check if keys belong to the same encryption algorithm
		if (
			( (pubKey instanceof RSAPublicKey) && !(privKey instanceof RSAPrivateKey) ) ||
			( (pubKey instanceof DSAPublicKey) && !(privKey instanceof DSAPrivateKey) ) ||
			( (pubKey instanceof ECPublicKey)  && !(privKey instanceof ECPrivateKey) )
			)
		{
			return null;
		}
		
		AsymmetricAlgorithm alg = null;
		// check if encryption algorithm is supported and assign the key type
		if ( privKey instanceof RSAPrivateKey )
		{
			alg = AsymmetricAlgorithm.RSA;
		}
		else if ( privKey instanceof DSAPrivateKey )
		{
			alg = AsymmetricAlgorithm.DSA;
		}
		else if ( privKey instanceof ECPrivateKey)
		{
			ECPublicKey ecPubKey = (ECPublicKey) pubKey;
			ECPrivateKey ecPrivKey = (ECPrivateKey) privKey;
			ECParameterSpec spec = ecPubKey.getParams();
						
			// Are both keys of the same EC domain?
			if ( false == spec.equals(ecPrivKey.getParams()) )
			{
				return null;
			}
			
			// EC domain must be one of the supported ones:
			if ( spec.equals(EcUtil.getSpec(AsymmetricAlgorithm.ECDSA_NISTP256)) )
			{
				alg = AsymmetricAlgorithm.ECDSA_NISTP256;
			}
			else if ( spec.equals(EcUtil.getSpec(AsymmetricAlgorithm.ECDSA_NISTP384)) )
			{
				alg = AsymmetricAlgorithm.ECDSA_NISTP384;
			}
			else if ( spec.equals(EcUtil.getSpec(AsymmetricAlgorithm.ECDSA_NISTP521)) )
			{
				alg = AsymmetricAlgorithm.ECDSA_NISTP521;
			}
			else
			{
				return null;
			}
			
			// check validity of the public key
			if ( false == EcUtil.validPublicKey(alg, ecPubKey.getW()) )
			{
				return null;
			}
		} // if ECPrivateKey
		else
		{
			return null;
		}
			
		// just in case..
		if ( null == alg )
		{
			return null;
		}
		
		// keys are OK, assign the class's properties
		KeyCreator kc = new KeyCreator();
		kc.prvkey = privKey;
		kc.pubkey = pubKey;
		kc.pktype = alg;
		
		return kc;
	}
	
	/**
	 * Creates a key pair by passing a KeyPair structure.
	 * 
	 * @param pair - key pair
	 * 
	 * @return an instance of a key or null if invalid pair
	 */
	public static KeyCreator createFromKeypair(KeyPair pair)
	{
		// sanity check
		if ( null==pair )
		{
			return null;
		}
				
		// derive both keys from the key pair and pass them to createFromKeys()
		return createFromKeys(pair.getPublic(), pair.getPrivate());
	}
	
	/**
	 * Returns the public key, encoded as an array of bytes.
	 * If this is not supported by the key type, null will be returned.
	 * The returned public key is DER encoded.
	 * 
	 * @return public key as an array of bytes or null if not available
	 */
	public byte[] getPublic()
	{
		if ( null==pubkey )
		{
			return null;
		}
		
		return pubkey.getEncoded();
	}
	
	/**
	 * Returns the private key, encoded as an array of bytes.
	 * If this is not supported by the key type, null will be returned.
	 * The returned private key is DER encoded.
	 * 
	 * @return private key as an array of bytes or null if not available
	 */
	public byte[] getPrivate()
	{
		if ( null==prvkey )
		{
			return null;
		}
		
		return prvkey.getEncoded();
	}
	
	/**
	 * Returns the public key.
	 * 
	 * @return public key or null if not available
	 */
	public PublicKey getPublicKey()
	{
		if ( null==pubkey )
		{
			return null;
		}
		
		return pubkey;
	}
	
	/**
	 * Returns the private key.
	 * 
	 * @return private key or null if not available
	 */
	public PrivateKey getPrivateKey()
	{
		if ( null==prvkey )
		{
			return null;
		}
		
		return prvkey;
	}
	
	/**
	 * @return type (i.e. encryption algorithm) of the key pair stored by the class
	 */
	public AsymmetricAlgorithm getType()
	{
		return pktype;
	}
}
