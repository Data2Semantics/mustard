 #PBS -lnodes=1:mem64gb -lwalltime=1:00:00:00
 module load stopos
 mkdir -p "$TMPDIR"/exp/am/
 cp $HOME/exp/mustard-experiments-0.0.1-SNAPSHOT.jar "$TMPDIR"/exp/am/
 cd "$TMPDIR"/exp/am/
 for ((i=1; i<=3; i++)) ; do
 (
  # add some sleep to avoid potential conflicts in copying the same dataset twice
  timer=( $i * 5 )
  sleep $timer
  for ((j=1; j<=4; j++)) ; do
     stopos next -p am_pool
     if [ "$STOPOS_RC" != "OK" ]; then
       break
     fi
	 a=( $STOPOS_VALUE )
	 d=${a[1]} # get the directory of the dataset
	 if [ ! -d "$TMPDIR/exp/am/$d" ]; then
		cp -r $HOME/exp/am2/dataset/$d "$TMPDIR"/exp/am/
	 fi
     eval "java -cp mustard-experiments-0.0.1-SNAPSHOT.jar org.data2semantics.mustard.experiments.cluster.ClusterExperiment -dataset AM $STOPOS_VALUE" 
     stopos remove -p am_pool
	 cp *.result $HOME/exp/am2/results/regular/
  done
) &
done
wait
