def primes = [73728946997,1378256713,33654889487,967838569,27866350149,60564530937,4085785280167,59789670733,7333775613,893798331,758373750389,25453453701,478678785,67867870211,178678678821,78787630454]
def c
Random rn = new Random()
long seed
def collisions = []
def n
for (int i in 0..10000)
{
   c = Calendar.instance
   seed = c.time.time + primes[i % primes.size()]
   //println "s: "+ seed
   //rnd = new Random(seed)
   //println "r: "+ 
   n = (rnd.nextInt(900000)+100000)
   if (collisions.contains(n)) println "col ${n}"
   else collisions << n
}

println  collisions.size()


String.metaClass.static.randomNumeric = { digits ->
  // static Random rn = new Random()
  // def seed = Calendar.instance.time.time
  // def n = (rnd.nextInt(900000)+100000)
  def alphabet = ['0','1','2','3','4','5','6','7','8','9']
  new Random().with {
    (1..digits).collect { alphabet[ nextInt( alphabet.size() ) ] }.join()
  }
}

println String.randomNumeric(5)

