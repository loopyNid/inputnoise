Server.default.waitForBoot({
/*
	goal:
	reload the sample every time the python script sends a completion message.
	Then feed the sample to a GrainBuf synthDef.

	steps:
	1. make synth def
*/
// SynthDef(\grain, {|amp = 0.5, trigFreq = 100, grainDur = 0.1,  filePos = 0.5, rate = 1, pan = 0|
// 	var src;
// 	src = GrainBuf.ar(1, Impulse.kr(trigFreq), SinOsc.kr(0.01)*grainDur, b.bufnum, rate, SinOsc.kr(0.1, mul: 0.43, add: filePos), pan, c.bufnum)!2;
// 	Out.ar(0, src * amp);
// }).add;
Server.default.waitForBoot({
	(
		~counter= false;
		b = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 1);
		c = Buffer.sendCollection(s, Env.sine().discretize);
		SynthDef(\grain, {|amp = 0.4, trigFreq = 100, grainDur = 0.1,  filePos = 0.5, rate = 1, pan = 0, phasorRate = 1|
			var src, env, envgen;
			env =
			src = GrainBuf.ar(1, Impulse.kr(trigFreq), SinOsc.kr(0.01)*grainDur, b.bufnum, rate, Phasor.kr(rate: phasorRate), pan, c.bufnum)!2;
			Out.ar(0, src * amp);
			"grain ADDED".postln;
		}).add;
		// j = Synth(\grain, {"j".postln});
		// k = Synth(\grain);

		OSCdef.new(\reciever,{| msg, time, addr, port|
			b.free;
			if(~counter,{
				~counter.postln;
				~counter=false;
			},
				{
					~counter.postln;
					~counter=true;
				}
			);
			b = Buffer.read(s, "/var/log/sc/noise.wav", bufnum: 1);
			j = Synth(\grain,[\grainDur, 1.0.linrand,\rate, 1.0.rand, \phasorRate, 1.0.rand]);
			msg.postln;
		},'/reloadSample', recvPort: 6666);
	)
});
});
