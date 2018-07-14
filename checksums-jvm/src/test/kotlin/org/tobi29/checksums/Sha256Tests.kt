/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.checksums

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.arrays.fromHexadecimal
import org.tobi29.arrays.toHexadecimal
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual
import org.tobi29.stdex.utf8ToArray

object Sha256Tests : Spek({
    describe("generating a sha256 checksum") {
        data(
            { a -> "computing the checksum of \"$a\"" },
            data(
                "",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            ),
            data(
                "pleb".utf8ToArray().toHexadecimal(),
                "12bc2ef4bbfe56b90bf6540d0131a60ecf4f2923afa99ffc0f7afb6ef1d262be"
            ),
            *randomTests
        ) { a, expected ->
            val ctx = ChecksumAlgorithm.Sha256.createContext()
            ctx.update(a.fromHexadecimal())
            val actual = ctx.finish()
            it("should return \"$expected\"") {
                expected shouldEqual actual.toHexadecimal()
            }
        }
    }
})

private val randomTests = arrayOf(
    data(
        "d3",
        "28969cdfa74a12c82f3bad960b0b000aca2ac329deea5c2328ebc6f2ba9802c1"
    ),
    data(
        "a7ce",
        "728a4a82cbdd7ed82c9248b7df8869d4fe41d663f84c394993c80cc20574a72f"
    ),
    data(
        "25b94f",
        "1204496e618fc6bcc50ff2d9b77c5c46ad9c8b4e1660d6ba4ff8565766a31aeb"
    ),
    data(
        "ef47579e",
        "9745a371c06d06c3c4b200bf0e5ba5bb6da09a76991c8857ab03dd8dabd9e5c5"
    ),
    data(
        "9c41aa80ad",
        "6ac4bb07b62352e04255cd003363071e255387f6d9b4d764fb9642ce047363d8"
    ),
    data(
        "a2c3aefbde39",
        "c9e1060645fe3bfa96bca091d8ae884ec221d3c6c0521ab2b3a6c1e6a5da7609"
    ),
    data(
        "ee635f074e36dd",
        "ba452681292c0a2e8aea2f5ba823c79fdf3620dc430625a18066a288fbe56d26"
    ),
    data(
        "0445c6236ff17d44",
        "1f3a919ec30395e57353c38b94f88d5967bb2c14d144a995563ee4efd02028a2"
    ),
    data(
        "f335d49271428b84f5",
        "76b9d85bad513f55654d064a50294ac904e5c77e3f4cce0a4aaf8e900eeadbbf"
    ),
    data(
        "b15ed0c82f5b111664e8",
        "eacc68ddcc7453c0484b5a5c1b8621bac830ac7cb5ff6f255d03c8b77f147f2c"
    ),
    data(
        "ff23757fe02743648170da",
        "2b5a33e83693c7ceef1b16b6dc20b73206be801b9ea58b7bb69ff8bce1437b3d"
    ),
    data(
        "4566a73ac2c8945b51b53822",
        "2f3a59476e88c11f4710566a1a9e51fd27cfb59a7f3872baf81dbc2b92294244"
    ),
    data(
        "616297584a620fe843d62acc7d",
        "94938b63e4faf30ac838b21994d2d2ccecdc803d32cedc8f6dc3f20261298d1d"
    ),
    data(
        "976f594fa51a1ca4ebf662855a72",
        "0c7a44d80fb720ba82d3c7621961036d35a5df78109f736e29959b6a21186340"
    ),
    data(
        "14ee1a8965bb881eb1d4dc87afe821",
        "640857b2c3d130c1015746491a6c7faf593d49d4f04ddf7d1a150d13b6f40616"
    ),
    data(
        "4d65ba194e135bdde4d82ba2a3601916",
        "6051356fe669d59d00595fade4db7df815f6b4a241709b39594b7e4e29b23a84"
    ),
    data(
        "f03b735053719d5cc3521c71a792746eb0",
        "e3db004c2c2e9fc9cc51c833e90619eb44047644365072646401ac177ab8122a"
    ),
    data(
        "96cda8e621ec42f937c30d74e015d3db2e38",
        "186cde869abc63c17d9c954780a9b2853eb3afe27351679a7bea7537e32836a7"
    ),
    data(
        "3f1f270d5673047028bd25074f1bf96dc33066",
        "363360ced24147cb9a08fe1cc2e6b300a99c4789cf16795bdcf38f76ffc07d4e"
    ),
    data(
        "69c84d525eaff98efa28e834221f0f8858975945",
        "63a48520908d5d09e137a436868e84f77efa4ec85ba4ff0d5618c96c70e11181"
    ),
    data(
        "942e129dc85435b79d156bb53ed6a238946df7ff29",
        "f7cda0e0276ad62944537bf3268d812d862a50a90393a644366a29c132001d54"
    ),
    data(
        "0a9a9bac583642b4355c5392ee395ace4da57c8386b9",
        "468bd5ca36ff29c5bbdc012e4d2a849129a3a1ece73f34a2f7ef0cb5fed7dd63"
    ),
    data(
        "8666a1c73edfcdc40a7cc0fbb8b2ea37a81bbea2f1f638",
        "a6a19d070c84fa59792575c32140599562ae529e25a55a6dd3bf949ed169720a"
    ),
    data(
        "bde7829d634af9fb71d2d93c64cd66510c9ff31da0b81477",
        "1dcb78f67e034f3ef862d23b13afeb92c1818c83031e4360772386311e5b7bdb"
    ),
    data(
        "6d7ca04951863b6f5fb61beb4cd1522fd756a40bf9cafc0656",
        "78ca70c5d03719e7836cea69e2c5d2a1ad4579cd3817d99f10ca71acab03c25f"
    ),
    data(
        "75ef9df885d521bf5b439111a187350489f32e03bb205cdf403f",
        "60a1f329d8d246bf12bae1dd6917463166c5e4d1cd00030f9cb46a15368e15e0"
    ),
    data(
        "52a2283630160ed3a23b390174ff008ee46e910280d9a2e2e5dafc",
        "6eba976270dc0bd85282fe2d560bdb2e777154c4d8d48090a4900bbc5231a68c"
    ),
    data(
        "064351287861340423a19739f5e1d804ed9e6bd3bc67ed09a17959a4",
        "05a81241f273f4e37d23e4e480efcc830090f98fc8a796b7aaab33c35ea4c8b7"
    ),
    data(
        "284105dba1d1dd9877b844d96ea1229d48f4b261b11821248e06090cb6",
        "f0d9889f91ecbc6f26b9a998750b0061841767e5066bca26c5352b39adbfa7db"
    ),
    data(
        "69572f01a67cd6572043521d87fde659546b4005d13b6b94ce35c964549d",
        "dac7b06c47633901917fa826fbd5e96d1054f576d841ade98f4196d7afe9c58d"
    ),
    data(
        "33e4efdf38567941a26792b84f92362ed0ff9a9e22220f99c3c746a13ee91a",
        "9e8deefe9d16d568632c5cd086267f4e800c7e2ed340690496887e4f1a3d5b01"
    ),
    data(
        "4a5a42a223a7f57c26397a1ae91098f71f5605302ec88048781342867d6119b4",
        "8d0409020287ef09c1d8c33482e4fb2d79e1cdc8788b77e0b529c09052838625"
    ),
    data(
        "5f57b7ea72e569c4ce5cb0429917d3462e5d9de413471b84ab9bac997085de5971",
        "62c6e535ea0431e7e5eec8c1c31d537a118bcfad5070e2d74b2a6a3d6bb8ea22"
    ),
    data(
        "7db6c87687140bbf1f49ce53942fd9b3dd63a8959205f38a14749771557788235261",
        "714f88d7f264b3be71cbcf338df3d11323751676df3100b476d7d12169702f55"
    ),
    data(
        "1834e03d04eca0bdfc405c41382106b33962b5299af80d9156a7404dac621aacdc57d0",
        "d0da4e2afcf46aeeac97fb4af68b22305f015469f37a317fd0b949ec97042b14"
    ),
    data(
        "1e5bcc4cdba6d519a0044a2ec1cb553f36faa6b58c146f042f67b24cc4178f587ba520a9",
        "18e841f98f0be6412f9f7528bbe99dcb8e62a0c650f179b9eb3439e16160aaa7"
    ),
    data(
        "cb0bd352872e423c22a44201b6953c2bb5c7054b80ae4ff7430644d0cde8f8f113e3b0ef2a",
        "bf1a5240a914dfb2c14137cff9eb746b32ec85f94ca0a1ae0e362d9fdb5ea908"
    ),
    data(
        "b4c794e9a4c42a0ef62f8bf4d02627daf1c48ae17730751ac5c60e640476a4fc075aeddc5eeb",
        "5005ed88f2e2ede39f5a173a0a06a85d37728d4e02e243394ce67372ddfa10c6"
    ),
    data(
        "09d6d87438c340e6d2ec7c31d93890b11a22cb796f2cdf0edb2ad6c5f0c194efe69089de510a5f",
        "bd91dd640cda8f7ca7bee700970b46dd803f3189ab3297c4fe3c06a71ae5e2a8"
    ),
    data(
        "154d3ede26373a7c6d25fef969cbf3c21c6648547b000bb25430250cb1d16b8eff0645b0eaf2ad4d",
        "fd8c9f556e6af2aff2e1eab028b56149c39ece9ae931763b664809e4c2f29fcc"
    ),
    data(
        "ec8e5c3de8d90acad5956f0e41f22944d0c3f0473ccb627e4fd44e77478b21ee36dd658ca42f736752",
        "222a32b24b7e7ab01f64c0859726d951809c302f40cd3c3aa88adb51d8eaecfb"
    ),
    data(
        "eaaf362e191c7577535df514bf639e5868926bd1bbd5643765d60cf1b9e298df2f47957e34e9908d3aae",
        "1405c1193e0efdccee197abfa496dc99555a9101ca5551efd407f67b2baa8633"
    ),
    data(
        "1836369a509ae12837d7155d6aa3074f4e463b1bf91384b8a1cb3224580fa2e494cffeba2b1277699abeb9",
        "acb16e7fc3cb896557e0fd0d730b6abe4e7c5f02db6152e84e1472a29cc33297"
    ),
    data(
        "81fdd4341c0b79ac2aed93f9eec6384568c5e2f31c30a62db64c6af283ed6ac373fba3009487e8bc5d7e20ee",
        "41913fd419b9ef3f5c7767749a436414e87309716780eb27a419a3e069f5333a"
    ),
    data(
        "0516b931fd2de1969bf69a4333e661e48d4e7206064f240ba12027940cf7cbed0d4e06e7498901cc49b20556b2",
        "93f5bb2a8906c42eda54fbcee56fef7913625382ec8356b68acd382b0481e4fe"
    ),
    data(
        "51d6d8e840018e3b752fd8d2d640a8433f5b3811e430d8c3d95756a01f78e7b6d6b1d018999115204809c1910b0b",
        "765a70ffe067f0cb3e82bab0b3c7003dc1edffd7d0dfe52f1ed952a87a8b0b74"
    ),
    data(
        "51180d7c61a0cd4d625c752d635f6463e001175b4106207a47dff281af4e4e7cb68d8b42b1651141ec320662415789",
        "1a6ba1d72e5ec4a33f26b45bc90c0bf92938952cab2832c57535569c9d3e230d"
    ),
    data(
        "b9dc89dfb6044cbd6efea5943cb21e68c2dd4129f8f128e751921ad755f362d7c07f22fb42578836d630a0cd5ad93649",
        "af0f0e1fb2363ff3b4bb7e64705688df71e06968316e9214a0d181485a40c4ab"
    ),
    data(
        "dac05aff6959e97468673f0136ffff7418fe060499557b4a581b7a2de4d7be98effcff21dfc34ad60b6ef765f424f971b4",
        "1be60cfd1e7b23ae2bcf5250db00ce4a9a5334bd65220a6fbc01c3c04881035e"
    ),
    data(
        "b179a7be9d9355b29db6fa0e9e89d9a1b7cf79221ed5192832f9c5a9ea4d218efcd7e254c7e5435d6706873c0c641ae1972d",
        "9385c5aa8fd6d9eb3f032438ddaf45a702eaed805c3ea1d4891ec919eba2ef5d"
    ),
    data(
        "76af5f3a18c127f877995de3772f9849df731d57353f750930f6194498a0c408388200c8ee7480883f9c8e3a262b13064951b6",
        "c450c1d5235f38917c33ea35a65d6ea0fe7b54710114786cdb81ecf9ff3bfc0f"
    ),
    data(
        "2d9bc30dd3d6b0c412887627ccab4d693f39e586e24e25cbe6f1fe11651946748b1b4cee0b02f73658ad6e26e509a9f0577eb178",
        "6c6bf6b9f7ec33161f8f2508d6ebd566becea5b349f5db508c86aba894f6857b"
    ),
    data(
        "63f2d354dcc90b79270bf1d2057f9de1fb117038123e27df453817b057e3c5f3043ff84b07bc1fbdcd2b74063aa7ea3e4e8c8ea2de",
        "0d2d74e9e843de3514a20a21f37550017c2202281486955161d331e803cb8685"
    ),
    data(
        "dd04aeacd7b7a4b30800c0d5f360a4880dec3cbf5983f9cd02ad95f5afb5c89f146bb692bd3e721e0d8672dbfb5f6c5d3cde9a7ab586",
        "fc3b279b09898e7ca70257e44dcd363d41897ad04cf5b7ccccae6f707af03941"
    ),
    data(
        "b55ab4710d381bd4acda445da3d81544ae6be4f8b95616faacf20841c8befe6705345138ac96bccad950b752478ffff5d191274f3fb564",
        "cb3f05e4412b6b3c3b65075b2cd11d1106a6deb2397312094de864ca34e51bed"
    ),
    data(
        "8bbb35c670cf5ea95b8354689227d177f0ea449faea3643317fe70842854a82d69da242d983d2d5fde4c63fe2d1f01985f32eee63ac00554",
        "8012c8144aad549baf2b39b7134fe6b4c5bdc6468c5df18380597614b4df9933"
    ),
    data(
        "0a90f7c362948109da750196dc6a27155498c438b69950be2b01aad002189a350c19dc3a4b937f0d469238b79cf2d563b7ff2f65f3fdf47bfc",
        "0e0731eac3465f02eb891aff4dc85aaef922116c93d1be869432832922f067c3"
    ),
    data(
        "d7866c9f1996e8601d18503f5fe9f7fbd1c4121f0d303736449bd1af404782da6c88f7c69223e09ab3ccd37b68cad2b48fec7b4cd5954d2d8840",
        "00b02ec47b6e54281116a54c4ac7b244442e1a77eaf26715fb7cf6b717003597"
    ),
    data(
        "774e4eb274f42d2cc2f8c9f5d7d136272712e0aad25bf9459fec25a20cc4c220fb4d0126f4b573ec806e23592cf7c2e065852960c69661c49a508f",
        "aa8cd26a6f35049a742c3fb885cb332669f47a915924c17610004908c61d39e9"
    ),
    data(
        "db8abc75203115083cc6f377435249b2bfff6d3489e8f676d60f66f3c1259c64a5e68e61546dd746f4a8e9316b16d5b715db8423cc70c569972a3e3f",
        "dbc60ba1a2f0984073fe2d39b1a2e5403e84ecd51ac97752429c37aa43eee7f0"
    ),
    data(
        "78ebecf6fe51cb69fe2d4003d11b9684dcafdc32ddaeb7240331aedb9bdc41fa42cb7e160388701800914fd8051a9346471181a91b2cc6f7b8c095857b",
        "1b122b268dacd268873269e34f46a88269278f239dc713f1170718016500648a"
    ),
    data(
        "1b54cf1e2b7b65bd544c24c584d3fb9bb53ca6925b91d036d7fb53aaad0fd2878f79214b05ca0690ddd4bfba3f7d1b3876b7e518736022450b374c3f12b7",
        "40ee04c2974cad3426730231028c9d2efab5a0dcb160ee7e801c323f94572b4d"
    ),
    data(
        "d725f8d823500aa6378d8944ccb17fea8504f44e5d5e1d86d9ac499de7768b383ff563708752d6d29443d850419c14b75809061efeb89fe90535290c53ddfd",
        "b93bdcf76ab676eb6b2b4d23237ffbc581c6b1ebeaf03bfc0e5fec29e22214cc"
    ),
    data(
        "8dc49c967b13646777f5e20758c5601eeadd1eef5964ab82c7eedc7ff1be42eea7ac421651a9031d1264610e6c4ce4c92536284d16a3a99c97d5857b0703678e",
        "3812de1ac5043de769f493b53def6a9b178bed7b7e48f4b1fa538b4cb93e5d12"
    ),
    data(
        "47252c6a39e89ced43bd822fc6d2a9fe93649d6731e1b00e7272676f7300f28891ada7f04f12dd0c6799ff69bbbb6650cb8b737e4b43bd598845bf6eadb6636103",
        "e1acaa34feab6ddd9cd2bf5bd3c8655180a9e1baa0639c0f3cccf35a56fc684a"
    )
)
