 #PBS -lnodes=1:mem64gb -lwalltime=10:00:00
 module load stopos
 mkdir -p "$TMPDIR"/exp/bgs/
 cp $HOME/exp/mustard-experiments-0.0.1-SNAPSHOT.jar "$TMPDIR"/exp/bgs/
 cd "$TMPDIR"/exp/bgs/
 for ((i=1; i<=3; i++)) ; do
 (
  # add some sleep to avoid potential conflicts in copying the same dataset twice
  timer=( $i * 5 )
  sleep $timer
  for ((j=1; j<=4; j++)) ; do
     stopos next -p bgs_pool
     if [ "$STOPOS_RC" != "OK" ]; then
       break
     fi
	 a=( $STOPOS_VALUE )
	 d=${a[1]} # get the directory of the dataset
	 if [ ! -d "$TMPDIR/exp/bgs/$d" ]; then
		cp -r $HOME/exp/bgs2/dataset/$d "$TMPDIR"/exp/bgs/
	 fi
     eval "java -cp mustard-experiments-0.0.1-SNAPSHOT.jar org.data2semantics.mustard.experiments.cluster.ClusterExperiment -dataset BGS $STOPOS_VALUE" 
     stopos remove -p bgs_pool
	 cp *.result $HOME/exp/bgs2/results/regular/
  done
) &
done
wait
