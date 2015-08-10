def durations = [
  "P10D",
  "PT30M",
  "P1Y2M10D",
  "P1Y2M10DT4H5M3S",
  "P1Y2M10DT4H5M3.555S",
  "P1.5Y2.5M10.5DT4H5M3.555S"
]

def keys = ['full', 'repeat', 'year', 'year_fraction', 'month', 'month_fraction', 'week', 'week_fraction', 'day', 'day_fraction', 'time', 'hours', 'hours_fraction', 'minutes', 'minutes_fraction', 'seconds', 'seconds_fraction']
def remove_suffix = ['year', 'month', 'week', 'day', 'hours',  'minutes', 'seconds']

//def regexp = /^(R\d*\/)?P(?:\d+(?:\.\d+)?Y)?(?:\d+(?:\.\d+)?M)?(?:\d+(?:\.\d+)?W)?(?:\d+(?:\.\d+)?D)?(?:T(?:\d+(?:\.\d+)?H)?(?:\d+(?:\.\d+)?M)?(?:\d+(?:\.\d+)?S)?)?$/
def regexp = /^(R\d*\/)?P(\d+(\.\d+)?Y)?(\d+(\.\d+)?M)?(\d+(\.\d+)?W)?(\d+(\.\d+)?D)?(T(\d+(\.\d+)?H)?(\d+(\.\d+)?M)?(\d+(\.\d+)?S)?)?$/

durations.each {

  def matcher = it =~ regexp
  
  def v = [keys, matcher[0]].transpose().collectEntries()
  
  def v1 = v.collectEntries{ k, val ->

     def avoid = ['full', 'time'].contains(k)
     if (avoid) return [k, val] // avoid time and entry entries
     else
     {
         if (val)
         {
            if (remove_suffix.contains(k)) return [k, new BigDecimal(val[0..-2])] // remove last character "D" / "Y" / etc
            return [k, new BigDecimal(val)] // fractions
         }
         return [k,val]
     }

  }
  
  println v
  println v1
}

def s = ".5"

println new BigDecimal(s)